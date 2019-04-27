package club.fdawei.datawatcher.plugin.injector

import club.fdawei.datawatcher.plugin.common.ClassInfoBox
import javassist.*
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.BooleanMemberValue
import javassist.bytecode.annotation.StringMemberValue
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess

class DataSourceHandler extends ClassHandler {

    private static final String TAG = 'DataSourceHandler'

    DataSourceHandler(InjectHelper helper) {
        super(helper)
    }

    void handle(File classFile, File dir) {
        if (classFile == null || !classFile.exists()) {
            return
        }
        def dataFieldsClassName = getClassNameFromFile(classFile, dir)
        def dataFieldsCtClass = helper.classPool.getCtClass(dataFieldsClassName)
        handleDataFieldsClass(dataFieldsCtClass, dir)
    }

    private void handleDataFieldsClass(CtClass dataFieldsCtClass, File dir) {
        def sourceField = dataFieldsCtClass.getDeclaredField(ClassInfoBox.DataFields.FIELD_SOURCE_NAME)
        def dataSourceCtClass = sourceField.type
        if (dataSourceCtClass.isFrozen()) {
            dataSourceCtClass.defrost()
        }
        addDataBinderField(dataSourceCtClass)
        addIDataSourceInterface(dataSourceCtClass)
        addGetDataBinderMethod(dataSourceCtClass)
        addGetAllFieldValueMethod(dataSourceCtClass, dataFieldsCtClass)
        hookAllSetterMethod(dataSourceCtClass, dataFieldsCtClass)
        dataSourceCtClass.getClassFile()
        dataSourceCtClass.writeFile(dir.absolutePath)
        dataSourceCtClass.detach()
    }

    private void addDataBinderField(CtClass ctClass) {
        def src = "private final ${ClassInfoBox.IDataBinder.NAME} dataBinder = new ${ClassInfoBox.DataBinder.NAME}(this);"
        def dataBinderCtField = CtField.make(src, ctClass)
        ctClass.addField(dataBinderCtField)
    }

    private void addIDataSourceInterface(CtClass ctClass) {
        def iDataSourceCtClass = helper.classPool.getCtClass(ClassInfoBox.IDataSource.NAME)
        ctClass.addInterface(iDataSourceCtClass)
    }

    private void addGetDataBinderMethod(CtClass ctClass) {
        def src = "public ${ClassInfoBox.IDataBinder.NAME} getDataBinder() {return dataBinder;}"
        def getDataBinderCtMethod = CtMethod.make(src, ctClass)
        ctClass.addMethod(getDataBinderCtMethod)
    }

    private void addGetAllFieldValueMethod(CtClass ctClass, CtClass fieldsCtClass) {
        def srcBuilder = new StringBuilder()
        srcBuilder.append("public ${ClassInfoBox.Map.NAME} getAllFieldValue() {")
        srcBuilder.append("${ClassInfoBox.Map.NAME} map = new ${ClassInfoBox.HashMap.NAME}();")
        CtClass stringCtClass = helper.classPool.getCtClass('java.lang.String')
        fieldsCtClass.declaredFields.findAll {
            ctField -> ClassInfoBox.DataFields.FIELD_SOURCE_NAME != ctField.name && ctField.type == stringCtClass && Modifier.isPublic(ctField.getModifiers())
        }.each {
            ctField ->
                def sourceCtField = ctClass.getDeclaredField(ctField.name)
                if (sourceCtField.type.isPrimitive()) {
                    def wrapperName = (sourceCtField.type as CtPrimitiveType).getWrapperName()
                    srcBuilder.append("map.put(${fieldsCtClass.name}.${ctField.name}, new ${wrapperName}(${sourceCtField.name}));")
                } else {
                    srcBuilder.append("map.put(${fieldsCtClass.name}.${ctField.name}, ${sourceCtField.name});")
                }
        }
        srcBuilder.append('return map;')
        srcBuilder.append('}')
        ctClass.addMethod(CtMethod.make(srcBuilder.toString(), ctClass))
    }

    private void hookAllSetterMethod(CtClass ctClass, CtClass fieldsCtClass) {
        Set<CtField> sourceFieldSet  = new HashSet<>()
        CtClass stringCtClass = helper.classPool.getCtClass(ClassInfoBox.LString.NAME)
        fieldsCtClass.declaredFields.findAll {
            ctField -> ctField.type == stringCtClass
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
                    hookSetter(ctMethod, sourceFieldSet, fieldsCtClass)
                } else {
                    def setterField = checkSetterAndFindField(ctMethod)
                    if (setterField != null) {
                        hookSetter(ctMethod, [setterField] as HashSet, fieldsCtClass)
                    }
                }
        }
    }

    private boolean isAutoFindSetter(CtClass ctClass) {
        def attributeInfo = ctClass.getClassFile().getAttribute(AnnotationsAttribute.invisibleTag)
        if (attributeInfo == null) {
            return false
        }
        def annotationsAttribute = attributeInfo as AnnotationsAttribute
        def annotation = annotationsAttribute.getAnnotation(ClassInfoBox.DataSource.NAME)
        if (annotation == null) {
            return false
        }
        def memberValue = annotation.getMemberValue(ClassInfoBox.DataSource.PROPERTY_AUTO_FIND_SETTER_NAME)
        if (memberValue == null) {
            return true
        }
        return (memberValue as BooleanMemberValue).value
    }

    /**
     * 判断是否是被@FieldSetter注解的Setter,是返回对应的CtField,否则返回null
     */
    private CtField checkSetterAndFindField(CtMethod ctMethod) {
        def attributeInfo = ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.invisibleTag)
        if (attributeInfo == null) {
            return null
        }
        def annotationsAttribute = attributeInfo as AnnotationsAttribute
        def fieldSetterAnnotation = annotationsAttribute.getAnnotation(ClassInfoBox.FieldSetter.NAME)
        if (fieldSetterAnnotation == null) {
            return null
        }
        def memberValue = fieldSetterAnnotation.getMemberValue(ClassInfoBox.FieldSetter.PROPERTY_FIELD_NAME)
        String setterFieldName = (memberValue as StringMemberValue).value
        return ctMethod.getDeclaringClass().getDeclaredField(setterFieldName)
    }

    private void hookSetter(CtMethod ctMethod, Set<CtField> sourceFieldSet, CtClass fieldsCtClass) {
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
                                .append("this.dataBinder.${ClassInfoBox.DataBinder.METHOD_ON_DATA_CHANGED_NAME}(${fieldsCtClass.name}.${f.field.name}, _oldValue, _newValue);")
                                .toString()
                    } else {
                        replaceSrc = new StringBuilder()
                                .append("${f.field.type.name} _oldValue = this.${f.field.name};")
                                .append("${f.field.type.name} _newValue = \$1;")
                                .append("\$proceed(\$\$);")
                                .append("this.dataBinder.${ClassInfoBox.DataBinder.METHOD_ON_DATA_CHANGED_NAME}(${fieldsCtClass.name}.${f.field.name}, _oldValue, _newValue);")
                                .toString()
                    }
                    f.replace(replaceSrc)
                }
            }
        })
    }
}
