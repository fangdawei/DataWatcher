package club.fdawei.datawatcher.perfectprocessor.common;

import com.squareup.javapoet.ClassName;

public class TypeBox {

    public static final ClassName I_DATA_SOURCE =
            ClassName.get("club.fdawei.datawatcher.api.data", "IDataSource");

    public static final ClassName I_DATA_BINDER =
            ClassName.get("club.fdawei.datawatcher.api.data", "IDataBinder");

    public static final ClassName DATA_BINDER =
            ClassName.get("club.fdawei.datawatcher.api.data", "DataBinder");

    public static final ClassName ABS_WATCHER_PROXY =
            ClassName.get("club.fdawei.datawatcher.api.watcher", "AbsWatcherProxy");

    public static final ClassName NOTIFY_PUBLISHER =
            ClassName.get("club.fdawei.datawatcher.api.watcher", "WatcherNotifyPublisher");

    public static final ClassName I_WATCHER_PROXY_CREATOR =
            ClassName.get("club.fdawei.datawatcher.api.watcher", "IWatcherProxyCreator");

    public static final ClassName CHANGE_EVENT =
            ClassName.get("club.fdawei.datawatcher.api.data", "ChangeEvent");
}
