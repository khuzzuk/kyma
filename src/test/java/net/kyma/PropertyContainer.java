package net.kyma;

public class PropertyContainer<T> {
    private T value;

    public boolean hasValue() {
        return value != null;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
