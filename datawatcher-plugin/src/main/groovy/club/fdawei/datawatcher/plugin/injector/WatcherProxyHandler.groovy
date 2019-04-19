package club.fdawei.datawatcher.plugin.injector

import club.fdawei.datawatcher.plugin.common.ClassInfoDef
import club.fdawei.datawatcher.plugin.log.PluginLogger
import javassist.CtClass
import javassist.CtField
import javassist.bytecode.AnnotationsAttribute
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
        def watcherTargetCtClass = watcherProxyCtClass.getDeclaredField(ClassInfoDef.WatcherProxy.FIELD_TARGET_NAME).type
        if (watcherProxyCtClass.isFrozen()) {
            watcherProxyCtClass.defrost()
        }
        Map<CtField, String> fieldBindKeyMap = new HashMap<>();
        watcherProxyCtClass.declaredFields.findAll() {
            ctField -> isBindKeyField(ctField)
        }.each {
            ctField ->
                def bindKey = getFieldBindKey(ctField, watcherTargetCtClass)
                if (bindKey != null) {
                    fieldBindKeyMap.put(ctField, bindKey)
                }
        }
        if (fieldBindKeyMap.size() > 0) {
            def srcBuilder = new StringBuilder()
            srcBuilder.append("{")
            for(Map.Entry<CtField, String> entry : fieldBindKeyMap.entrySet()) {
                srcBuilder.append("${entry.key.name} = \"${entry.value}\";")
            }
            srcBuilder.append("}")
            def initBindKeysMethod = watcherProxyCtClass.getDeclaredMethod(ClassInfoDef.WatcherProxy.INIT_BINDKEYS_METHOD_NAME)
            if (initBindKeysMethod != null) {
                initBindKeysMethod.setBody(srcBuilder.toString())
            }
        }
        watcherProxyCtClass.writeFile(dir.absolutePath)
        watcherProxyCtClass.detach()
    }

    private boolean isBindKeyField(CtField field) {
        if (!field.name.endsWith(ClassInfoDef.WatcherProxy.BINDKEY_FIELD_NAME_SUFFIX)) {
            return false
        }
        if (field.type != helper.classPool.getCtClass('java.lang.String')) {
            return false
        }
        return true
    }

    private String getFieldBindKey(CtField ctField, CtClass watcherTargetCtClass) {
        def changeEventCtClass = helper.classPool.getCtClass(ClassInfoDef.ChangeEvent.NAME)
        def dataWatchMethodName = ctField.name.replace(ClassInfoDef.WatcherProxy.BINDKEY_FIELD_NAME_SUFFIX, '')
        def dataWatchMethod = watcherTargetCtClass.getDeclaredMethod(dataWatchMethodName, [changeEventCtClass] as CtClass[])
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
        def dataWatchAnnotation = annotationsAttribute.getAnnotation(ClassInfoDef.DataWatch.NAME)
        if (dataWatchAnnotation == null) {
            PluginLogger.e(TAG, "method ${dataWatchMethodName} without @DataWatch")
            return null
        }
        def memberValue = dataWatchAnnotation.getMemberValue(ClassInfoDef.DataWatch.PROPERTY_FIELD_NAME)
        return (memberValue as StringMemberValue).value
    }
}
