package club.fdawei.datawatcher.api.data;

public class ChangeEvent<SOURCE, FIELD> {

    private SOURCE source;
    private FIELD oldValue;
    private FIELD newValue;

    private ChangeEvent() {
    }

    public SOURCE getSource() {
        return source;
    }

    public FIELD getOldValue() {
        return oldValue;
    }

    public FIELD getNewValue() {
        return newValue;
    }

    @SuppressWarnings("unchecked")
    public static <S, F> ChangeEvent<S, F> obtainChangeEvent(Object source, Object oldValue, Object newValue) {
        ChangeEvent<S, F> changeEvent = new ChangeEvent<>();
        changeEvent.source = ((S) source);
        changeEvent.oldValue = ((F) oldValue);
        changeEvent.newValue = ((F) newValue);
        return changeEvent;
    }
}
