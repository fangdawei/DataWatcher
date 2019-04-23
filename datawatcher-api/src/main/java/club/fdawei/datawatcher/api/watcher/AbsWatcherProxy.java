package club.fdawei.datawatcher.api.watcher;


import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public abstract class AbsWatcherProxy<TARGET> implements IWatcherProxy {

    private WeakReference<TARGET> targetRef;
    private final Map<String, List<WatcherNotifyPublisher>> publisherMap = new HashMap<>();

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
        List<WatcherNotifyPublisher> publisherList = publisherMap.get(fieldKey);
        if (publisherList == null) {
            publisherList = new LinkedList<>();
            publisherMap.put(fieldKey, publisherList);
        }
        publisherList.add(publisher);
    }

    @Override
    public void onDataChanged(Object source, String fieldKey, Object oldValue, Object newValue) {
        dispatchDataChange(source, fieldKey, oldValue, newValue);
    }

    @Override
    public void onDataBind(Object source, String fieldKey, Object value) {
        dispatchDataBind(source, fieldKey, value);
    }

    private void dispatchDataChange(Object source, String fieldKey, Object oldValue, Object newValue) {
        if (!isTargetAlive()) {
            return;
        }
        List<WatcherNotifyPublisher> publisherList = publisherMap.get(fieldKey);
        if (publisherList != null) {
            for (WatcherNotifyPublisher publisher : publisherList) {
                publisher.notifyWatcher(source, oldValue, newValue);
            }
        }
    }

    private void dispatchDataBind(Object source, String fieldKey, Object value) {
        List<WatcherNotifyPublisher> publisherList = publisherMap.get(fieldKey);
        if (publisherList != null) {
            for (WatcherNotifyPublisher publisher : publisherList) {
                if (publisher.isNeedNotifyWhenBind()) {
                    publisher.notifyWatcher(source, null, value);
                }
            }
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
    public boolean equals(Object obj) {
        if (obj instanceof AbsWatcherProxy) {
            return this.targetRef.get() == ((AbsWatcherProxy) obj).targetRef.get();
        } else {
            return false;
        }
    }
}
