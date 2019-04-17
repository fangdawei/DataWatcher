package club.fdawei.datawatcher.processor.common;

public final class GenClassInfoDef {

    public static final class DataFields {
        public static final String NAME_PREFIX = "fields_";
        public static final String FIELD_SOURCE_NAME = "_source_";
    }

    public static final class WatcherProxy {
        public static final String NAME_SUFFIX = "_WatcherProxy";
        public static final String FIELD_TARGET_NAME = "_target_";
    }

    public static final class WatcherProxyCreator {
        public static final String NAME_SUFFIX = "_WatcherProxyCreator";
    }
}
