package club.fdawei.datawatcher.api.data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import club.fdawei.datawatcher.api.watcher.IWatcherProxy;
import club.fdawei.datawatcher.api.watcher.WatcherProxyFactory;

public class DataBinder implements IDataBinder {

    private IDataSource source;
    private final Set<IWatcherProxy> watcherSet = new CopyOnWriteArraySet<>();

    public DataBinder(IDataSource source) {
        this.source = source;
    }

    @Override
    public void notifyWatcher(String fieldKey, Object oldValue, Object newValue) {
        if (!isValueChanged(oldValue, newValue)) {
            return;
        }
        for (IWatcherProxy watcher : watcherSet) {
            watcher.onDataChanged(source, fieldKey, oldValue, newValue);
        }
    }

    private void notifyWatcherWhenBind(IWatcherProxy watcher) {
        Map<String, Object> allFieldValueMap = source.getAllFieldValue();
        if (allFieldValueMap == null) {
            return;
        }
        for (Map.Entry<String, Object> fieldEntry : allFieldValueMap.entrySet()) {
            if (watcher.needNotifyWhenBind(fieldEntry.getKey())) {
                watcher.onDataChanged(source, fieldEntry.getKey(), null, fieldEntry.getValue());
            }
        }
    }

    @Override
    public void addWatcher(Object target) {
        if (target == null) {
            return;
        }
        IWatcherProxy watcher = WatcherProxyFactory.createWatcherProxy(target);
        if (watcher == null) {
            return;
        }
        if (watcherSet.add(watcher)) {
            notifyWatcherWhenBind(watcher);
        }
    }

    @Override
    public void removeWatcher(Object target) {
        if (target == null) {
            return;
        }
        for(IWatcherProxy watcherProxy : watcherSet) {
            if (watcherProxy.isBlongTarget(target)) {
                watcherSet.remove(watcherProxy);
            } else if (!watcherProxy.isTargetAlive()) {
                watcherSet.remove(watcherProxy);
            }
        }
    }

    @Override
    public boolean hasWatcher() {
        return watcherSet.size() > 0;
    }

    @Override
    public void clearWatcher() {
        watcherSet.clear();
    }

    private boolean isValueChanged(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) {
            return false;
        } else if (oldValue == null) {
            return true;
        } else {
            return !oldValue.equals(newValue);
        }
    }
}
