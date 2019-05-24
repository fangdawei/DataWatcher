package club.fdawei.datawatcher.plugin.injector


import javassist.ClassPath
import javassist.ClassPool

class InjectorImpl implements IInjector {

    private static final String TAG = 'Injector'

    private final ClassPool classPool = new ClassPool(true)
    private final Collection<ClassPath> classPathList = new LinkedList<>()
    private final ClassHandler dataHandler = new DataClassHandler(this)
    private final ClassHandler watcherHandler = new WatcherClassHandler(this)

    @Override
    void addClassPath(List<String> pathList) {
        if (pathList == null) {
            return
        }
        for(String path : pathList) {
            def classPath = classPool.appendClassPath(path)
            classPathList.add(classPath)
        }
    }

    @Override
    void inject(InjectInfo injectInfo) {
        if (!isInjectInfoValid(injectInfo)) {
            return
        }
        if (injectInfo.entityList == null) {
            return
        }
        injectInfo.entityList.each {
            def classFile = new File(injectInfo.sourceDir, it.name)
            switch (it.type) {
                case InjectEntityInfo.Type.DATA_FIELDS:
                    dataHandler.handle(classFile, injectInfo.sourceDir)
                    break
                case InjectEntityInfo.Type.WATCHER_PROXY:
                    watcherHandler.handle(classFile, injectInfo.sourceDir)
                    break
            }
        }
    }

    @Override
    void clear() {
        classPathList.each {
            classPool.removeClassPath(it)
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