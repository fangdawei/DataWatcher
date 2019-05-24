package club.fdawei.datawatcher.plugin.injector


class InjectInfo {

    private File sourceDir
    private List<InjectEntityInfo> entityList
    private File destFile
    private Type type

    InjectInfo(File sourceDir, File destFile, Type type) {
        this.sourceDir = sourceDir
        this.destFile = destFile
        this.type = type
    }

    void setEntityList(List<InjectEntityInfo> list) {
        this.entityList = list
    }

    File getSourceDir() {
        return sourceDir
    }

    List<InjectEntityInfo> getEntityList() {
        return entityList
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
                ", entityList size=" + (entityList == null ? 0 : entityList.size()) +
                ", type=" + type +
                ", destFile=" + destFile.absolutePath +
                '}'
    }

    enum Type {
        JAR, DIR, FILE_LIST
    }
}