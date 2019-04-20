package club.fdawei.datawatcher.plugin.injector


class InjectInfo {

    private File sourceDir
    private List<InjectClassInfo> classInfoList
    private File destFile
    private Type type

    InjectInfo(File sourceDir, File destFile, Type type) {
        this.sourceDir = sourceDir
        this.destFile = destFile
        this.type = type
    }

    void setClassInfoList(List<InjectClassInfo> classInfoList) {
        this.classInfoList = classInfoList
    }

    File getSourceDir() {
        return sourceDir
    }

    List<InjectClassInfo> getClassInfoList() {
        return classInfoList
    }

    File getDestFile() {
        return destFile
    }

    Type getType() {
        return type
    }

    @Override
    String toString() {
        return "InjectInfo{" +
                "sourceDir=" + sourceDir +
                ", classInfoList size=" + (classInfoList == null ? 0 : classInfoList.size()) +
                ", type=" + type +
                ", destFile=" + destFile.absolutePath +
                '}'
    }

    enum Type {
        JAR, DIR, FILE_LIST
    }
}