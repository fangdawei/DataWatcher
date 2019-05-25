package club.fdawei.datawatcher.api.watcher;

/**
 * Created by david on 2019/4/3.
 */
public interface IWatcherProxy {
    void onDataChange(Object source, String field, Object oldValue, Object newValue);

    void onBindData(Object source, String field, Object value);

    boolean isTargetAlive();
}
