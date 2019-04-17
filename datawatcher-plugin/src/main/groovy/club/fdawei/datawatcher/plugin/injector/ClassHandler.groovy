package club.fdawei.datawatcher.plugin.injector

class ClassHandler {

    protected InjectHelper helper

    ClassHandler(InjectHelper helper) {
        this.helper = helper
    }

    static String getClassNameFromFile(File classFile, File dir) {
        def dirPath
        if (dir.absolutePath.endsWith(File.separator)) {
            dirPath = dir.absolutePath
        } else {
            dirPath = dir.absolutePath + File.separator
        }
        def className = classFile.absolutePath
                .replace(dirPath, '')
                .replace(File.separator, '.')
                .replace('.class', '')
        return className
    }
}