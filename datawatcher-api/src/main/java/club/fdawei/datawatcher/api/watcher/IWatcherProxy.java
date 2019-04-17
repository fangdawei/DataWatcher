package club.fdawei.datawatcher.api.watcher;

/**
 * Created by david on 2019/4/3.
 */
public interface IWatcherProxy {
    void onDataChanged(Object source, String fieldKey, Object oldValue, Object newValue);

    boolean needNotifyWhenBind(String fieldKey);

    boolean isBlongTarget(Object target);

    boolean isTargetAlive();
}
