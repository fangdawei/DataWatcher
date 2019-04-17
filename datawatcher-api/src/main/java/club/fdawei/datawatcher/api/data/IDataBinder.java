package club.fdawei.datawatcher.api.data;


public interface IDataBinder {

    void notifyWatcher(String fieldKey, Object oldValue, Object newValue);

    void addWatcher(Object target);

    void removeWatcher(Object target);

    boolean hasWatcher();

    void clearWatcher();
}
