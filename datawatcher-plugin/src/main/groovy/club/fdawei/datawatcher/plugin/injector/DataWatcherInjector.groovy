package club.fdawei.datawatcher.plugin.injector

import javassist.ClassPath
import javassist.ClassPool


class DataWatcherInjector implements InjectHelper {

    private static final String TAG = 'DataWatcherInjector'

    private final ClassPool classPool = new ClassPool(true)
    private final Collection<ClassPath> classPathList = new LinkedList<>()
    private final DataSourceHandler dataSourceHandler = new DataSourceHandler(this)
    private final WatcherProxyHandler watcherProxyHandler = new WatcherProxyHandler(this)

    void addClassPath(List<String> pathList) {
        if (pathList == null) {
            return
        }
        for(String path : pathList) {
            def classPath = classPool.appendClassPath(path)
            classPathList.add(classPath)
        }
    }

    void inject(InjectInfo injectInfo) {
        if (!isInjectInfoValid(injectInfo)) {
            return
        }
        if (injectInfo.classInfoList == null) {
            return
        }
        injectInfo.classInfoList.each {
            classInfo ->
                def classFile = new File(injectInfo.sourceDir, classInfo.name)
                switch (classInfo.type) {
                    case InjectClassInfo.Type.DATAFIELDS:
                        dataSourceHandler.handle(classFile, injectInfo.sourceDir)
                        break
                    case InjectClassInfo.Type.WATCHERPROXY:
                        watcherProxyHandler.handle(classFile, injectInfo.sourceDir)
                        break
                }
        }
    }

    void clear() {
        classPathList.each {
            classPath ->
                classPool.removeClassPath(classPath)
        }
        classPathList.clear()
    }

    @Override
    ClassPool getClassPool() {
        return classPool
    }

    private static boolean isInjectInfoValid(InjectInfo injectInfo) {
        if (injectInfo == null) {
            return false
        }
        if (injectInfo.sourceDir == null || !injectInfo.sourceDir.exists()) {
            return false
        }
        return true
    }
}