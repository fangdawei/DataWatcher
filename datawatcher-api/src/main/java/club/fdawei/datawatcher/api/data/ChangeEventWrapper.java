package club.fdawei.datawatcher.api.data;

public class ChangeEventWrapper<SOURCE, FIELD> extends ChangeEvent<SOURCE, FIELD> {

    public void setSource(SOURCE source) {
        this.source = source;
    }

    public void setOldValue(FIELD oldValue) {
        this.oldValue = oldValue;
    }

    public void setNewValue(FIELD newValue) {
        this.newValue = newValue;
    }
}
