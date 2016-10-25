package net.kyma;

import pl.khuzzuk.messaging.BagMessage;

//TODO refactor and move this from kyma to MessageBus module.

public class ReturnMessage<T> implements BagMessage<T> {
    private T message;
    private String type;
    private String returnType;
    @Override
    public BagMessage<T> setMessage(T message) {
        this.message = message;
        return this;
    }

    @Override
    public BagMessage<T> setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public T getMessage() {
        return message;
    }

    public String getReturnType() {
        return returnType;
    }

    public ReturnMessage<T> setReturnType(String returnType) {
        this.returnType = returnType;
        return this;
    }
}
