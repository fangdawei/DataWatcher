package club.fdawei.datawatcher.api.data;

public class ChangeEvent<SOURCE, FIELD> {

    protected SOURCE source;
    protected FIELD oldValue;
    protected FIELD newValue;

    public SOURCE getSource() {
        return source;
    }

    public FIELD getOldValue() {
        return oldValue;
    }

    public FIELD getNewValue() {
        return newValue;
    }
}
