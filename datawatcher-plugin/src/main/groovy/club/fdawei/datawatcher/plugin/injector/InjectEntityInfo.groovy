package club.fdawei.datawatcher.plugin.injector


class InjectEntityInfo {

    private String name
    private Type type

    InjectEntityInfo(String name, Type type) {
        this.name = name
        this.type = type
    }

    String getName() {
        return name
    }

    Type getType() {
        return type
    }

    @Override
    String toString() {
        return "InjectEntityInfo{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}'
    }

    enum Type {
        DATA_FIELDS, WATCHER_PROXY
    }
}