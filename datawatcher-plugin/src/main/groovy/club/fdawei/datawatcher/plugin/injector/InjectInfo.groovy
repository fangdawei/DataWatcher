package club.fdawei.datawatcher.plugin.injector


class InjectInfo {

    private File sourceDir
    private File destFile
    private Type type
    private Set<InjectEntity> entitySet = new LinkedHashSet<>()

    InjectInfo(File sourceDir, File destFile, Type type) {
        this.sourceDir = sourceDir
        this.destFile = destFile
        this.type = type
    }

    void setEntities(List<InjectEntity> list) {
        this.entitySet.clear()
        this.entitySet.addAll(list)
    }

    File getSourceDir() {
        return sourceDir
    }

    Collection<InjectEntity> getEntities() {
        return entitySet
    }

    File getDestFile() {
        return destFile
    }

    Type getType() {
        return type
    }

    @Override
    String toString() {
        def builder = new StringBuilder()
        builder.append("InjectInfo{" +
                "sourceDir=" + sourceDir +
                ", entities size=" + entitySet.size() +
                ", type=" + type +
                ", destFile=" + destFile.absolutePath +
                '}')
        entitySet.each {
            builder.append("\n${it}")
        }
        return builder.toString()
    }

    enum Type {
        JAR, DIR, FILE_LIST
    }
}