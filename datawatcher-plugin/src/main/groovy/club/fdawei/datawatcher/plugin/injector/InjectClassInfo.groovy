package club.fdawei.datawatcher.plugin.injector

class InjectClassInfo {

    private String name
    private Type type

    InjectClassInfo(String name, Type type) {
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
        return "InjectClassInfo{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}'
    }


    enum Type {
        DATA_FIELDS
    }
}