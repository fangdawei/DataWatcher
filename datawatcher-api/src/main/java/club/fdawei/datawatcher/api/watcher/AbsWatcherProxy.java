package club.fdawei.datawatcher.api.watcher;


import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public abstract class AbsWatcherProxy<TARGET> implements IWatcherProxy {

    private WeakReference<TARGET> targetRef;
    private final Map<Class<?>, Map<String, List<WatcherNotifyPublisher>>> publisherMap = new LinkedHashMap<>();

    public AbsWatcherProxy(TARGET target) {
        this.targetRef = new WeakReference<>(target);
        initPublishers();
    }

    @Nullable
    protected TARGET getTarget() {
        return targetRef.get();
    }

    protected abstract void initPublishers();

    protected void registerPublisher(Class<?> clz, String field, WatcherNotifyPublisher publisher) {
        if (field == null || field.isEmpty() || publisher == null) {
            return;
        }
        Map<String, List<WatcherNotifyPublisher>> fieldPublisherMap = publisherMap.get(clz);
        if (fieldPublisherMap == null) {
            fieldPublisherMap = new LinkedHashMap<>();
            publisherMap.put(clz, fieldPublisherMap);
        }
        List<WatcherNotifyPublisher> publisherList = fieldPublisherMap.get(field);
        if (publisherList == null) {
            publisherList = new LinkedList<>();
            fieldPublisherMap.put(field, publisherList);
        }
        publisherList.add(publisher);
    }

    @Override
    public void onDataChange(Object source, String fieldKey, Object oldValue, Object newValue) {
        dispatchDataChange(source, fieldKey, oldValue, newValue);
    }

    @Override
    public void onBindData(Object source, String field, Object value) {
        dispatchBindData(source, field, value);
    }

    private void dispatchDataChange(Object source, String field, Object oldValue, Object newValue) {
        if (!isTargetAlive()) {
            return;
        }
        List<WatcherNotifyPublisher> publisherList = findPublishers(source, field);
        if (publisherList == null) {
            return;
        }
        for (WatcherNotifyPublisher publisher : publisherList) {
            publisher.notifyWatcher(source, oldValue, newValue);
        }
    }

    private void dispatchBindData(Object source, String field, Object value) {
        if (!isTargetAlive()) {
            return;
        }
        List<WatcherNotifyPublisher> publisherList = findPublishers(source, field);
        if (publisherList == null) {
            return;
        }
        for (WatcherNotifyPublisher publisher : publisherList) {
            if (publisher.isNeedNotifyWhenBind()) {
                publisher.notifyWatcher(source, null, value);
            }
        }
    }

    private List<WatcherNotifyPublisher> findPublishers(Object source, String field) {
        Map<String, List<WatcherNotifyPublisher>> fieldPublisherMap = publisherMap.get(source.getClass());
        if (fieldPublisherMap == null) {
            return null;
        }
        return fieldPublisherMap.get(field);
    }

    @Override
    public boolean isTargetAlive() {
        return targetRef.get() != null;
    }
}
