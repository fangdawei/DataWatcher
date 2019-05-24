package club.fdawei.datawatcher.plugin.injector

import javassist.ClassPool

interface IInjector {
    ClassPool getClassPool()

    void addClassPath(List<String> pathList)

    void inject(InjectInfo injectInfo)

    void clear()
}