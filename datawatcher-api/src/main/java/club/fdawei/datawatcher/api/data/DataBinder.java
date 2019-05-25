package club.fdawei.datawatcher.api.data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import club.fdawei.datawatcher.api.watcher.IWatcherProxy;

public class DataBinder implements IDataBinder {

    private IDataSource source;
    private final Set<IWatcherProxy> watcherSet = new CopyOnWriteArraySet<>();

    public DataBinder(IDataSource source) {
        this.source = source;
    }

    @Override
    public void onDataChanged(String field, Object oldValue, Object newValue) {
        if (!isValueChanged(oldValue, newValue)) {
            return;
        }
        notifyWatcher(field, oldValue, newValue);
    }

    private void notifyWatcher(String field, Object oldValue, Object newValue) {
        for (IWatcherProxy watcher : watcherSet) {
            watcher.onDataChange(source, field, oldValue, newValue);
        }
    }

    private void notifyWatcherWhenBind(IWatcherProxy watcher) {
        Map<String, Object> allFieldValueMap = source.getAllFieldValue();
        if (allFieldValueMap == null) {
            return;
        }
        for (Map.Entry<String, Object> fieldEntry : allFieldValueMap.entrySet()) {
            watcher.onBindData(source, fieldEntry.getKey(), fieldEntry.getValue());
        }
    }

    @Override
    public void addWatcher(IWatcherProxy watcher) {
        if (watcher == null) {
            return;
        }
        if (watcherSet.add(watcher)) {
            notifyWatcherWhenBind(watcher);
        }
    }

    @Override
    public void removeWatcher(IWatcherProxy watcher) {
        if (watcher == null) {
            return;
        }
        for(IWatcherProxy watcherProxy : watcherSet) {
            if (watcher == watcherProxy) {
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
