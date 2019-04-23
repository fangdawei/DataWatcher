package club.fdawei.datawatcher.plugin.common


/**
 * Created by david on 2019/4/11.
 */
final class ClassInfoBox {

    static final class DataFields {
        static final String NAME_PREFIX = 'fields_'
        static final String FIELD_SOURCE_NAME = '_source_'

        static boolean isDataFields(String fileName) {
            return fileName ==~ /^.*fields_.+\.class$/
        }
    }

    static final class WatcherProxy {
        static final String NAME_SUFFIX = '_WatcherProxy'
        static final String FIELD_TARGET_NAME = '_target_'
        static final String BINDKEY_FIELD_NAME_SUFFIX = '_BindKey'
        static final String INIT_BINDKEYS_METHOD_NAME = 'initBindKeys'

        static boolean isWatcherProxy(String fileName) {
            return fileName ==~ /^.*_WatcherProxy\.class$/
        }
    }

    static final class IDataSource {
        static final String NAME = 'club.fdawei.datawatcher.api.data.IDataSource'
    }

    static final class IDataBinder {
        static final String NAME = 'club.fdawei.datawatcher.api.data.IDataBinder'
    }

    static final class DataBinder {
        static final String NAME = 'club.fdawei.datawatcher.api.data.DataBinder'
        static final String METHOD_ON_DATA_CHANGED_NAME = 'onDataChanged'
    }

    static final class ChangeEvent {
        static final String NAME = 'club.fdawei.datawatcher.api.data.ChangeEvent'
    }

    static final class DataSource {
        static final String NAME = 'club.fdawei.datawatcher.annotation.DataSource'
        static final String PROPERTY_AUTO_FIND_SETTER_NAME = 'setterAutoFind'
    }

    static final class DataWatch {
        static final String NAME = 'club.fdawei.datawatcher.annotation.DataWatch'
        static final String PROPERTY_FIELD_NAME = 'field'
    }

    static final class FieldSetter {
        static final String NAME = 'club.fdawei.datawatcher.annotation.FieldSetter'
        static final String PROPERTY_FIELD_NAME = 'field'
    }

    static final class Map {
        static final String NAME = 'java.util.Map'
    }

    static final class HashMap {
        static final String NAME = 'java.util.HashMap'
    }

    static final class LString {
        static final String NAME = 'java.lang.String'
    }
}
