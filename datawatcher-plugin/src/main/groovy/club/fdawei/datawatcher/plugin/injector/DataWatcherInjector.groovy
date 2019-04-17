package club.fdawei.datawatcher.plugin.injector

import club.fdawei.datawatcher.plugin.common.ClassInfoDef
import club.fdawei.datawatcher.plugin.log.PluginLogger
import javassist.ClassPath
import javassist.ClassPool

import java.util.jar.JarFile

class DataWatcherInjector implements InjectHelper {

    private static final String TAG = 'DataWatcherInjector'

    private final ClassPool classPool = new ClassPool(true)
    private final Collection<ClassPath> classPathList = new LinkedList<>()
    private final DataSourceHandler dataSourceHandler = new DataSourceHandler(this)
    private final WatcherProxyHandler watcherProxyHandler = new WatcherProxyHandler(this)

    DataWatcherInjector() {

    }

    void addClassPath(String pathName) {
        def classPath = classPool.appendClassPath(pathName)
        classPathList.add(classPath)
    }

    void inject(InjectInfo injectInfo) {
        if (!isInjectInfoValid(injectInfo)) {
            return
        }
        PluginLogger.i(TAG, "inject ${injectInfo.sourceDir}")
        def inputDir = injectInfo.sourceDir
        if (inputDir.isDirectory()) {
            inputDir.eachFileRecurse {
                file ->
                    if (file.isDirectory()) {
                        return
                    } else if (ClassInfoDef.DataFields.isDataFields(file.name)) {
                        dataSourceHandler.handle(file, inputDir)
                    } else if (ClassInfoDef.WatcherProxy.isWatcherProxy(file.name)) {
                        watcherProxyHandler.handle(file, inputDir)
                    }
            }
        }
    }

    private boolean isInjectInfoValid(InjectInfo injectInfo) {
        if (injectInfo == null) {
            return false
        }
        if (injectInfo.sourceDir == null || !injectInfo.sourceDir.exists()) {
            return false
        }
        return true
    }

    void clear() {
        classPathList.each {
            classPool.removeClassPath()
        }
        classPathList.clear()
    }

    @Override
    ClassPool getClassPool() {
        return classPool
    }

    boolean checkJarNeedInject(String jarPath) {
        def isNeedInject = false
        def jarFile = new JarFile(jarPath)
        def entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            def jarEntry = entries.nextElement()
            if (jarEntry.isDirectory()) {
                continue
            }
            if (ClassInfoDef.DataFields.isDataFields(jarEntry.name)
                    || ClassInfoDef.WatcherProxy.isWatcherProxy(jarEntry.name)) {
                isNeedInject = true
                break
            }
        }
        return isNeedInject
    }
}