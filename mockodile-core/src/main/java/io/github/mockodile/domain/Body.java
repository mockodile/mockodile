package io.github.mockodile.domain;

public class Body<T> {

    private final T bodyObject;
    private final String bodyString;
    private final byte[] bodyRaw;

    public Body(T bodyObject) {
        this.bodyObject = bodyObject;
        this.bodyString = null;
        this.bodyRaw = null;
    }

    public Body(String bodyString) {
        this.bodyObject = null;
        this.bodyString = bodyString;
        this.bodyRaw = null;
    }

    public Body(byte[] bodyRaw) {
        this.bodyObject = null;
        this.bodyString = null;
        this.bodyRaw = bodyRaw;
    }

    public boolean isRaw() {
        return bodyRaw != null;
    }

    public boolean isString() {
        return bodyString != null;
    }

    public T getObject() {
        return bodyObject;
    }

    public String getString() {
        return bodyString;
    }

    public byte[] getRaw() {
        return bodyRaw;
    }
}
