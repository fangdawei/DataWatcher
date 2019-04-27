package club.fdawei.datawatcher.plugin.injector

import club.fdawei.datawatcher.plugin.common.ClassInfoBox
import club.fdawei.datawatcher.plugin.log.PluginLogger
import javassist.CtClass
import javassist.CtField
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.Descriptor
import javassist.bytecode.annotation.ClassMemberValue
import javassist.bytecode.annotation.StringMemberValue

class WatcherProxyHandler extends ClassHandler {

    private static final String TAG = 'WatcherProxyHandler'

    WatcherProxyHandler(InjectHelper helper) {
        super(helper)
    }

    void handle(File classFile, File dir) {
        if (classFile == null || !classFile.exists()) {
            return
        }
        def watcherProxyClassName = getClassNameFromFile(classFile, dir)
        def watcherProxyCtClass = helper.classPool.getCtClass(watcherProxyClassName)
        def watcherTargetCtClass = watcherProxyCtClass.getDeclaredField(ClassInfoBox.WatcherProxy.FIELD_TARGET_NAME).type
        if (watcherProxyCtClass.isFrozen()) {
            watcherProxyCtClass.defrost()
        }
        Map<CtField, String> fieldBindKeyMap = new HashMap<>()
        watcherProxyCtClass.declaredFields.findAll() {
            isBindKeyField(it)
        }.each {
            def bindKey = getBindKeyFieldValue(it, watcherTargetCtClass)
            if (bindKey != null) {
                fieldBindKeyMap.put(it, bindKey)
            }
        }
        if (fieldBindKeyMap.size() > 0) {
            def srcBuilder = new StringBuilder()
            srcBuilder.append("{")
            fieldBindKeyMap.each {
                srcBuilder.append("${it.key.name} = \"${it.value}\";")
            }
            srcBuilder.append("}")
            def initBindKeysMethod = watcherProxyCtClass.getDeclaredMethod(ClassInfoBox.WatcherProxy.INIT_BINDKEYS_METHOD_NAME)
            if (initBindKeysMethod != null) {
                initBindKeysMethod.setBody(srcBuilder.toString())
            }
        }
        watcherProxyCtClass.writeFile(dir.absolutePath)
        watcherProxyCtClass.detach()
    }

    private boolean isBindKeyField(CtField field) {
        if (!field.name.endsWith(ClassInfoBox.WatcherProxy.BINDKEY_FIELD_NAME_SUFFIX)) {
            return false
        }
        if (field.type != helper.classPool.getCtClass(ClassInfoBox.LString.NAME)) {
            return false
        }
        return true
    }

    private String getBindKeyFieldValue(CtField ctField, CtClass watcherTargetCtClass) {
        def changeEventCtClass = helper.classPool.getCtClass(ClassInfoBox.ChangeEvent.NAME)
        def dataWatchMethodName = ctField.name.replace(ClassInfoBox.WatcherProxy.BINDKEY_FIELD_NAME_SUFFIX, '')
        final String dataWatchMethodDesc = Descriptor.ofMethod(CtClass.voidType, [changeEventCtClass] as CtClass[])
        def dataWatchMethod = watcherTargetCtClass.getMethod(dataWatchMethodName, dataWatchMethodDesc)
        if (dataWatchMethod == null) {
            PluginLogger.e(TAG, "method ${dataWatchMethodName} not found in ${watcherTargetCtClass.simpleName}")
            return null
        }
        def attributeInfo = dataWatchMethod.getMethodInfo().getAttribute(AnnotationsAttribute.invisibleTag)
        if (attributeInfo == null) {
            PluginLogger.e(TAG, "method ${dataWatchMethodName} no ${AnnotationsAttribute.invisibleTag}")
            return null
        }
        def annotationsAttribute = attributeInfo as AnnotationsAttribute
        def dataWatchAnnotation = annotationsAttribute.getAnnotation(ClassInfoBox.DataWatch.NAME)
        if (dataWatchAnnotation == null) {
            PluginLogger.e(TAG, "method ${dataWatchMethodName} without @DataWatch")
            return null
        }
        def fieldMemberValue = dataWatchAnnotation.getMemberValue(ClassInfoBox.DataWatch.PROPERTY_FIELD_NAME) as StringMemberValue
        def dataMemberValue = dataWatchAnnotation.getMemberValue(ClassInfoBox.DataWatch.PROPERTY_DATA_NAME) as ClassMemberValue
        String fieldValue
        if (dataMemberValue != null && dataMemberValue.value != ClassInfoBox.LObject.NAME) {
            fieldValue = dataMemberValue.value.replace('$', '.') + '.' + fieldMemberValue.value
        } else {
            fieldValue = fieldMemberValue.value
        }
        return fieldValue
    }
}
