package club.fdawei.datawatcher.plugin.injector

import javassist.ClassPath
import javassist.ClassPool


class Injector implements InjectHelper {

    private static final String TAG = 'Injector'

    private final ClassPool classPool = new ClassPool(true)
    private final Collection<ClassPath> classPathList = new LinkedList<>()
    private final DataSourceHandler dataSourceHandler = new DataSourceHandler(this)

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
            def classFile = new File(injectInfo.sourceDir, it.name)
            switch (it.type) {
                case InjectClassInfo.Type.DATA_FIELDS:
                    dataSourceHandler.handle(classFile, injectInfo.sourceDir)
                    break
            }
        }
    }

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