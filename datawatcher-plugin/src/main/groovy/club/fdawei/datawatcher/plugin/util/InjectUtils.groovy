package club.fdawei.datawatcher.plugin.util

import club.fdawei.datawatcher.plugin.common.ClassBox
import club.fdawei.datawatcher.plugin.injector.InjectEntity
import com.android.utils.FileUtils

import java.util.jar.JarFile

import static club.fdawei.datawatcher.plugin.injector.InjectEntity.Type.DATA_FIELDS
import static club.fdawei.datawatcher.plugin.injector.InjectEntity.Type.WATCHER_PROXY

/**
 * Created by david on 2019/05/22.
 */
final class InjectUtils {

    /**
     * 根据名称获取被注入类的类型
     * @param name
     * @return {@link InjectEntity.Type}
     */
    static InjectEntity.Type getInjectEntityType(String name) {
        if (ClassBox.DataFields.isDataFields(name)) {
            return DATA_FIELDS
        } else if (ClassBox.WatcherProxy.isWatcherProxy(name)) {
            return WATCHER_PROXY
        }
        return null
    }

    /**
     * 判断类文件是否需要注入
     * @param classFile 类文件
     * @param dir 类文件所在目录
     * @return 需要注入返回{@link InjectEntity}, 否则返回null
     */
    static InjectEntity collectFromClassFile(File classFile, File dir) {
        def type = getInjectEntityType(classFile.name)
        if (type != null) {
            return new InjectEntity(FileUtils.relativePath(classFile, dir), type)
        } else {
            def fieldsFile = new File(ClassBox.DataFields.getPathFromSource(classFile))
            if (fieldsFile.exists()) {
                return new InjectEntity(FileUtils.relativePath(fieldsFile, dir), DATA_FIELDS)
            }
            def proxyFile = new File(ClassBox.WatcherProxy.getPathFromTarget(classFile))
            if (proxyFile.exists()) {
                return new InjectEntity(FileUtils.relativePath(proxyFile, dir), WATCHER_PROXY)
            }
        }
        return null
    }

    /**
     * 从目录中查找需要注入的类文件
     * @param dir 待查找目录
     * @return 返回所有需要注入的{@link InjectEntity}
     */
    static List<InjectEntity> collectFromDir(File dir) {
        final List<InjectEntity> list = new LinkedList<>()
        if (dir.exists()) {
            dir.eachFileRecurse {
                file ->
                    if (file.directory) {
                        return
                    }
                    def type = getInjectEntityType(file.name)
                    if (type == null) {
                        return
                    }
                    list.add(new InjectEntity(FileUtils.relativePath(file, dir), type))
            }
        }
        return list
    }

    /**
     * 从jar包中查找需要注入的类文件
     * @param jarPath jar包的文件路径
     * @return 返回所有需要注入的{@link InjectEntity}
     */
    static List<InjectEntity> collectFromJar(String jarPath) {
        def jarFile = new JarFile(jarPath)
        def entries = jarFile.entries()
        final List<InjectEntity> list = new LinkedList<>()
        while (entries.hasMoreElements()) {
            def jarEntry = entries.nextElement()
            if (jarEntry.isDirectory()) {
                continue
            }
            def type = getInjectEntityType(jarEntry.name)
            if (type == null) {
                continue
            }
            list.add(new InjectEntity(jarEntry.name, type))
        }
        return list
    }
}
