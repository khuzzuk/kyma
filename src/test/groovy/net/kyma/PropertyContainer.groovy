package net.kyma

class PropertyContainer<T> {
    private T value

    boolean hasValue() {
        value != null
    }

    T getValue() {
        value
    }

    void setValue(T value) {
        this.value = value
    }
}
