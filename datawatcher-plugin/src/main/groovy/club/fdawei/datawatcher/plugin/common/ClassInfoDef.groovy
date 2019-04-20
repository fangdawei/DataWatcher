package club.fdawei.datawatcher.plugin.common


/**
 * Created by david on 2019/4/11.
 */
class ClassInfoDef {

    static class DataFields {
        static final String NAME_PREFIX = 'fields_'
        static final String FIELD_SOURCE_NAME = '_source_'

        static boolean isDataFields(String fileName) {
            return fileName ==~ /^.*fields_.+\.class$/
        }
    }

    static class WatcherProxy {
        static final String NAME_SUFFIX = '_WatcherProxy'
        static final String FIELD_TARGET_NAME = '_target_'
        static final String BINDKEY_FIELD_NAME_SUFFIX = '_BindKey'
        static final String INIT_BINDKEYS_METHOD_NAME = 'initBindKeys'

        static boolean isWatcherProxy(String fileName) {
            return fileName ==~ /^.*_WatcherProxy\.class$/
        }
    }

    static class IDataSource {
        static final String NAME = 'club.fdawei.datawatcher.api.data.IDataSource'
    }

    static class IDataBinder {
        static final String NAME = 'club.fdawei.datawatcher.api.data.IDataBinder'
    }

    static class DataBinder {
        static final String NAME = 'club.fdawei.datawatcher.api.data.DataBinder'
    }

    static class ChangeEvent {
        static final String NAME = 'club.fdawei.datawatcher.api.data.ChangeEvent'
    }

    static class DataSource {
        static final String NAME = 'club.fdawei.datawatcher.annotation.DataSource'
        static final String PROPERTY_AUTO_FIND_SETTER_NAME = 'autoFindSetter'
    }

    static class DataWatch {
        static final String NAME = 'club.fdawei.datawatcher.annotation.DataWatch'
        static final String PROPERTY_FIELD_NAME = 'field'
    }

    static class FieldSetter {
        static final String NAME = 'club.fdawei.datawatcher.annotation.FieldSetter'
        static final String PROPERTY_FIELD_NAME = 'field'
    }

    static class Map {
        static final String NAME = 'java.util.Map'
    }

    static class HashMap {
        static final String NAME = 'java.util.HashMap'
    }

    static class LString {
        static final String NAME = 'java.lang.String'
    }
}
