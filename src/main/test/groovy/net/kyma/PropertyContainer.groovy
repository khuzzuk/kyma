package net.kyma

class PropertyContainer<T> {
    T value

    void setValue(T value) {
        this.value = value
    }

    boolean hasValue() {
        return value != null
    }
}
