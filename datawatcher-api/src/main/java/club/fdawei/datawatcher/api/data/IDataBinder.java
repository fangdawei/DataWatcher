package club.fdawei.datawatcher.api.data;


public interface IDataBinder {

    void onDataChanged(String fieldKey, Object oldValue, Object newValue);

    void addWatcher(Object target);

    void removeWatcher(Object target);

    boolean hasWatcher();

    void clearWatcher();
}
