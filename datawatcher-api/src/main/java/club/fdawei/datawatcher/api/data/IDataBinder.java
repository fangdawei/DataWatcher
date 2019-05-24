package club.fdawei.datawatcher.api.data;

import club.fdawei.datawatcher.api.watcher.IWatcherProxy;

public interface IDataBinder {

    void onDataChanged(String fieldKey, Object oldValue, Object newValue);

    void addWatcher(IWatcherProxy watcher);

    void removeWatcher(IWatcherProxy watcher);

    boolean hasWatcher();

    void clearWatcher();
}
