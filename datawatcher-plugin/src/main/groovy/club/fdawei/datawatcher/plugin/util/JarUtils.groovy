package club.fdawei.datawatcher.plugin.util


final class JarUtils {

    /**
     * 解压jar包
     * @param srcPath
     * @param destPath
     */
    static void unzipJarFile(String srcPath, String destPath) {
        def ant = new AntBuilder()
        ant.invokeMethod("unzip", [src: srcPath, dest: destPath, overwrite: 'true'])
    }

    /**
     * 压缩生成jar包
     * @param srcPath
     * @param destPath
     */
    static void zipJarFile(String srcPath, String destPath) {
        def ant = new AntBuilder()
        ant.invokeMethod("zip", [basedir: srcPath, destfile: destPath])
    }
}
