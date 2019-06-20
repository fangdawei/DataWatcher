package club.fdawei.datawatcher.plugin.common


/**
 * Created by david on 2019/4/11.
 */
final class ClassBox {

    static final class DataFields {
        static final String NAME_PREFIX = 'fields_'
        static final String FIELD_SOURCE = '_source_'

        static boolean isDataFields(String fileName) {
            return fileName ==~ /^.*fields_.+\.class$/
        }

        static final String ANNO_TYPE_NAME = 'club.fdawei.datawatcher.annotation.DataFields'

        static String getPathFromSource(File sourceFile) {
            def parent = sourceFile.parent
            def parentPath = parent != null ? parent : ""
            return "${parentPath}${File.separator}${NAME_PREFIX}${sourceFile.name}"
        }
    }

    static final class WatcherProxy {
        static final String NAME_SUFFIX = '_WatcherProxy'
        static final String FIELD_TARGET = '_target_'

        static boolean isWatcherProxy(String fileName) {
            return fileName ==~ /^.*_WatcherProxy\.class$/
        }

        static final String ANNO_TYPE_NAME = 'club.fdawei.datawatcher.annotation.WatcherProxy'

        static String getPathFromTarget(File targetFile) {
            return targetFile.absolutePath.replace('$', '_').concat(NAME_SUFFIX)
        }
    }

    static final class IDataSource {
        static final String NAME = 'club.fdawei.datawatcher.api.data.IDataSource'
        static final String FIELD_DATA_BINDER = '_data_binder_'
        static final String METHOD_GET_DATA_BINDER = 'getDataBinder'
        static final String METHOD_GET_ALL_FIELD_VALUE = 'getAllFieldValue'
    }

    static final class IDataBinder {
        static final String NAME = 'club.fdawei.datawatcher.api.data.IDataBinder'
    }

    static final class DataBinder {
        static final String NAME = 'club.fdawei.datawatcher.api.data.DataBinder'
        static final String METHOD_ON_DATA_CHANGED = 'onDataChanged'
    }

    static final class IWatcherTarget {
        static final String NAME = 'club.fdawei.datawatcher.api.watcher.IWatcherTarget'
        static final String FIELD_WATCHER_PROXY = '_watcher_proxy_'
        static final String METHOD_GET_WATCHER_PROXY = 'getWatcherProxy'
    }

    static final class IWatcherProxy {
        static final String NMAE = 'club.fdawei.datawatcher.api.watcher.IWatcherProxy'
    }

    static final class ChangeEvent {
        static final String NAME = 'club.fdawei.datawatcher.api.data.ChangeEvent'
    }

    static final class DataSource {
        static final String NAME = 'club.fdawei.datawatcher.annotation.DataSource'
        static final String PROPERTY_AUTO_FIND_SETTER = 'setterAutoFind'
    }

    static final class FieldSetter {
        static final String NAME = 'club.fdawei.datawatcher.annotation.FieldSetter'
        static final String PROPERTY_FIELD = 'field'
    }

    static final class Map {
        static final String NAME = 'java.util.Map'
    }

    static final class HashMap {
        static final String NAME = 'java.util.HashMap'
    }

    static final class LObject {
        static final String NAME = 'java.lang.Object'
    }

    static final class LString {
        static final String NAME = 'java.lang.String'
    }
}
