package club.fdawei.datawatcher.plugin.util


class JarUtils {

    static void unzipJarFile(String srcPath, String destPath) {
        def ant = new AntBuilder()
        ant.invokeMethod("unzip", [src: srcPath, dest: destPath, overwrite: 'true'])
    }

    static void zipJarFile(String srcPath, String destPath) {
        def ant = new AntBuilder()
        ant.invokeMethod("zip", [basedir: srcPath, destfile: destPath])
    }
}
