package club.fdawei.datawatcher.api.watcher;


import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;


public abstract class AbsWatcherProxy<TARGET> implements IWatcherProxy {

    private WeakReference<TARGET> targetRef;
    private final Map<String, WatcherNotifyPublisher> publisherMap = new HashMap<>();

    public AbsWatcherProxy(TARGET target) {
        this.targetRef = new WeakReference<>(target);
        initBindKeys();
        initPublishers();
    }

    @Nullable
    protected TARGET getTarget() {
        return targetRef.get();
    }

    protected abstract void initBindKeys();

    protected abstract void initPublishers();

    protected void registerPublisher(String fieldKey, WatcherNotifyPublisher publisher) {
        if (fieldKey == null || fieldKey.isEmpty()) {
            return;
        }
        if (publisher == null) {
            return;
        }
        publisherMap.put(fieldKey, publisher);
    }

    @Override
    public void onDataChanged(Object source, String fieldKey, Object oldValue, Object newValue) {
        dispatchDataChange(source, fieldKey, oldValue, newValue);
    }

    public void dispatchDataChange(Object source, String fieldKey, Object oldValue, Object newValue) {
        if (!isTargetAlive()) {
            return;
        }
        WatcherNotifyPublisher publisher = publisherMap.get(fieldKey);
        if (publisher != null) {
            publisher.notifyWatcher(source, oldValue, newValue);
        }
    }

    @Override
    public boolean isBlongTarget(Object target) {
        if (targetRef.get() == null) {
            return false;
        }
        return targetRef.get() == target;
    }

    @Override
    public boolean isTargetAlive() {
        return targetRef.get() != null;
    }

    @Override
    public boolean needNotifyWhenBind(String fieldKey) {
        WatcherNotifyPublisher publisher = publisherMap.get(fieldKey);
        if (publisher == null) {
            return true;
        }
        return publisher.isNeedNotifyWhenBind();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbsWatcherProxy) {
            return this.targetRef.get() == ((AbsWatcherProxy) obj).targetRef.get();
        } else {
            return false;
        }
    }
}
