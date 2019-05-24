package club.fdawei.datawatcher.plugin.injector

import club.fdawei.datawatcher.plugin.common.ClassBox
import club.fdawei.datawatcher.plugin.util.ClassUtils
import javassist.*
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.BooleanMemberValue
import javassist.bytecode.annotation.StringMemberValue
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess

class DataClassHandler extends ClassHandler {

    private static final String TAG = 'DataClassHandler'

    DataClassHandler(IInjector injector) {
        super(injector)
    }

    @Override
    void handle(File classFile, File dir) {
        if (classFile == null || !classFile.exists()) {
            return
        }
        def fieldsClassName = ClassUtils.getClassNameFromFile(classFile, dir)
        def fieldsCtClass = injector.classPool.getCtClass(fieldsClassName)
        if (dataFieldsValid(fieldsCtClass)) {
            handleDataFieldsClass(fieldsCtClass, dir)
        }
    }

    private boolean dataFieldsValid(CtClass dataFields) {
        return ClassUtils.isCtClassWithAnno(dataFields, ClassBox.DataFields.ANNO_TYPE_NAME, false)
    }

    private void handleDataFieldsClass(CtClass dataFieldsCtClass, File dir) {
        def sourceField = dataFieldsCtClass.getDeclaredField(ClassBox.DataFields.FIELD_SOURCE)
        def sourceCtClass = sourceField.type
        if (sourceCtClass.isFrozen()) {
            sourceCtClass.defrost()
        }
        addDataBinderField(sourceCtClass)
        addIDataSourceInterface(sourceCtClass)
        addGetDataBinderMethod(sourceCtClass)
        addGetAllFieldValueMethod(sourceCtClass, dataFieldsCtClass)
        hookAllSetterMethod(sourceCtClass, dataFieldsCtClass)
        sourceCtClass.writeFile(dir.absolutePath)
        sourceCtClass.detach()
    }

    private void addDataBinderField(CtClass ctClass) {
        def src = "private final ${ClassBox.IDataBinder.NAME} ${ClassBox.IDataSource.FIELD_DATA_BINDER} " +
                "= new ${ClassBox.DataBinder.NAME}(this);"
        def dataBinderCtField = CtField.make(src, ctClass)
        ctClass.addField(dataBinderCtField)
    }

    private void addIDataSourceInterface(CtClass ctClass) {
        ctClass.addInterface(injector.classPool.getCtClass(ClassBox.IDataSource.NAME))
    }

    private void addGetDataBinderMethod(CtClass ctClass) {
        def src = "public ${ClassBox.IDataBinder.NAME} ${ClassBox.IDataSource.METHOD_GET_DATA_BINDER}() " +
                "{return ${ClassBox.IDataSource.FIELD_DATA_BINDER};}"
        def getDataBinderCtMethod = CtMethod.make(src, ctClass)
        ctClass.addMethod(getDataBinderCtMethod)
    }

    private void addGetAllFieldValueMethod(CtClass ctClass, CtClass fieldsCtClass) {
        def srcBuilder = new StringBuilder()
        srcBuilder.append("public ${ClassBox.Map.NAME} ${ClassBox.IDataSource.METHOD_GET_ALL_FIELD_VALUE}() {")
        srcBuilder.append("${ClassBox.Map.NAME} map = new ${ClassBox.HashMap.NAME}();")
        fieldsCtClass.declaredFields.findAll {
            ctField -> isFieldKey(ctField)
        }.each {
            ctField ->
                def sourceCtField = ctClass.getDeclaredField(ctField.name)
                if (sourceCtField.type.isPrimitive()) {
                    def wrapperName = (sourceCtField.type as CtPrimitiveType).getWrapperName()
                    srcBuilder.append("map.put(\"${sourceCtField.name}\", new ${wrapperName}(${sourceCtField.name}));")
                } else {
                    srcBuilder.append("map.put(\"${sourceCtField.name}\", ${sourceCtField.name});")
                }
        }
        srcBuilder.append('return map;')
        srcBuilder.append('}')
        ctClass.addMethod(CtMethod.make(srcBuilder.toString(), ctClass))
    }

    private void hookAllSetterMethod(CtClass ctClass, CtClass fieldsCtClass) {
        Set<CtField> sourceFieldSet  = new HashSet<>()
        fieldsCtClass.declaredFields.findAll {
            ctField -> isFieldKey(ctField)
        }.each {
            ctField ->
                def dataSourceCtField = ctClass.getDeclaredField(ctField.name)
                if (dataSourceCtField != null) {
                    sourceFieldSet.add(dataSourceCtField)
                }
        }
        def autoFindSetter = isAutoFindSetter(ctClass)
        ctClass.declaredMethods.each {
            ctMethod ->
                if (autoFindSetter) {
                    hookSetter(ctMethod, sourceFieldSet)
                } else {
                    hookSetterIfAnnoWith(ctMethod)
                }
        }
    }

    private boolean isFieldKey(CtField ctField) {
        CtClass stringCtClass = injector.classPool.getCtClass(ClassBox.LString.NAME)
        return ctField.name != ClassBox.DataFields.FIELD_SOURCE &&
                ctField.type == stringCtClass &&
                Modifier.isPublic(ctField.getModifiers())
    }

    private boolean isAutoFindSetter(CtClass ctClass) {
        def attributeInfo = ctClass.getClassFile().getAttribute(AnnotationsAttribute.invisibleTag)
        if (attributeInfo == null) {
            return false
        }
        def annotationsAttribute = attributeInfo as AnnotationsAttribute
        def annotation = annotationsAttribute.getAnnotation(ClassBox.DataSource.NAME)
        if (annotation == null) {
            return false
        }
        def memberValue = annotation.getMemberValue(ClassBox.DataSource.PROPERTY_AUTO_FIND_SETTER)
        if (memberValue == null) {
            return true
        }
        return (memberValue as BooleanMemberValue).value
    }

    private void hookSetter(CtMethod ctMethod, Set<CtField> sourceFieldSet) {
        ctMethod.instrument(new ExprEditor() {
            @Override
            void edit(FieldAccess f) throws CannotCompileException {
                if (f.writer && sourceFieldSet.contains(f.field)) {
                    String replaceSrc
                    if (f.field.type.primitive) {
                        def wrapperTypeName = (f.field.type as CtPrimitiveType).wrapperName
                        replaceSrc = new StringBuilder()
                                .append("${wrapperTypeName} _oldValue = new ${wrapperTypeName}(this.${f.field.name});")
                                .append("${wrapperTypeName} _newValue = new ${wrapperTypeName}(\$1);")
                                .append("\$proceed(\$\$);")
                                .append("this.${ClassBox.IDataSource.FIELD_DATA_BINDER}" +
                                ".${ClassBox.DataBinder.METHOD_ON_DATA_CHANGED}" +
                                "(\"${f.field.name}\", _oldValue, _newValue);")
                                .toString()
                    } else {
                        replaceSrc = new StringBuilder()
                                .append("${f.field.type.name} _oldValue = this.${f.field.name};")
                                .append("${f.field.type.name} _newValue = \$1;")
                                .append("\$proceed(\$\$);")
                                .append("this.${ClassBox.IDataSource.FIELD_DATA_BINDER}" +
                                ".${ClassBox.DataBinder.METHOD_ON_DATA_CHANGED}" +
                                "(\"${f.field.name}\", _oldValue, _newValue);")
                                .toString()
                    }
                    f.replace(replaceSrc)
                }
            }
        })
    }

    private void hookSetterIfAnnoWith(CtMethod ctMethod) {
        def field = getFieldIfSetterAnnoWith(ctMethod)
        if (field != null) {
            hookSetter(ctMethod, [setterField] as HashSet)
        }
    }

    /**
     * 判断是否被@FieldSetter注解,是:返回对应的CtField,否:返回null
     */
    private CtField getFieldIfSetterAnnoWith(CtMethod ctMethod) {
        def attributeInfo = ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.invisibleTag)
        if (attributeInfo == null) {
            return null
        }
        def annotationsAttribute = attributeInfo as AnnotationsAttribute
        def fieldSetterAnnotation = annotationsAttribute.getAnnotation(ClassBox.FieldSetter.NAME)
        if (fieldSetterAnnotation == null) {
            return null
        }
        def memberValue = fieldSetterAnnotation.getMemberValue(ClassBox.FieldSetter.PROPERTY_FIELD)
        String setterFieldName = (memberValue as StringMemberValue).value
        return ctMethod.getDeclaringClass().getDeclaredField(setterFieldName)
    }
}
