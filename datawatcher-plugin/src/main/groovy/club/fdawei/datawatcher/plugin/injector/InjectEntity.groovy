package club.fdawei.datawatcher.plugin.injector

import com.android.annotations.NonNull


class InjectEntity {

    private String name
    private Type type

    InjectEntity(@NonNull String name, @NonNull Type type) {
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
        return "InjectEntity{" +
                "name=" + name +
                ", type=" + type +
                '}'
    }

    @Override
    int hashCode() {
        if (name != null) {
            return name.hashCode()
        }
        return 0
    }

    @Override
    boolean equals(Object obj) {
        if (obj instanceof InjectEntity) {
            return obj.name == this.name
        } else {
            return false
        }
    }

    enum Type {
        DATA_FIELDS, WATCHER_PROXY
    }
}