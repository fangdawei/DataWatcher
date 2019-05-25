package club.fdawei.datawatcher.plugin.injector

import club.fdawei.datawatcher.plugin.common.ClassBox
import club.fdawei.datawatcher.plugin.util.ClassUtils
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod

class WatcherClassHandler extends ClassHandler {

    private static final String TAG = 'WatcherClassHandler'

    WatcherClassHandler(IInjector injector) {
        super(injector)
    }

    @Override
    void handle(File classFile, File dir) {
        if (classFile == null || !classFile.exists()) {
            return
        }
        def proxyClassName = ClassUtils.getClassNameFromFile(classFile, dir)
        def proxyCtClass = injector.classPool.getCtClass(proxyClassName)
        if (watcherProxyValid(proxyCtClass)) {
            handleWatcherProxyClass(proxyCtClass, dir)
        }
    }

    private boolean watcherProxyValid(CtClass watcherProxy) {
        return ClassUtils.isCtClassWithAnno(watcherProxy, ClassBox.WatcherProxy.ANNO_TYPE_NAME, false)
    }

    private void handleWatcherProxyClass(CtClass watcherProxyCtClass, File dir) {
        def targetField = watcherProxyCtClass.getDeclaredField(ClassBox.WatcherProxy.FIELD_TARGET)
        def targetCtClass = targetField.type
        if (targetCtClass.frozen) {
            targetCtClass.defrost()
        }
        addIWatcherTargetInterface(targetCtClass)
        addWatcherProxyField(targetCtClass, watcherProxyCtClass)
        addGetWatcherProxyMethod(targetCtClass)
        targetCtClass.writeFile(dir.absolutePath)
        targetCtClass.detach()
    }

    private void addIWatcherTargetInterface(CtClass targetCtClass) {
        targetCtClass.addInterface(injector.classPool.getCtClass(ClassBox.IWatcherTarget.NAME))
    }

    private void addWatcherProxyField(CtClass targetCtClass, CtClass proxyCtClass) {
        def srcBuilder = new StringBuilder()
        srcBuilder.append("private ${ClassBox.IWatcherProxy.NMAE} " +
                "${ClassBox.IWatcherTarget.FIELD_WATCHER_PROXY} = " +
                "new ${proxyCtClass.name}(this);")
        def ctField = CtField.make(srcBuilder.toString(), targetCtClass)
        targetCtClass.addField(ctField)
    }

    private void addGetWatcherProxyMethod(CtClass targetCtClass) {
        def srcBuilder = new StringBuilder()
        srcBuilder.append("public ${ClassBox.IWatcherProxy.NMAE} " +
                "${ClassBox.IWatcherTarget.METHOD_GET_WATCHER_PROXY}() {")
        srcBuilder.append("return this.${ClassBox.IWatcherTarget.FIELD_WATCHER_PROXY};")
        srcBuilder.append("}")
        def ctMethod = CtMethod.make(srcBuilder.toString(), targetCtClass)
        targetCtClass.addMethod(ctMethod)
    }
}