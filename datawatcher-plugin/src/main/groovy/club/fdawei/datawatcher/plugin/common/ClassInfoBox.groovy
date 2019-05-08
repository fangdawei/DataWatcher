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

    static final class LObject {
        static final String NAME = 'java.lang.Object'
    }

    static final class LString {
        static final String NAME = 'java.lang.String'
    }
}
