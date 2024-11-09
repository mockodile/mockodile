package io.github.mockodile.domain;

public class RequestCount {

    public enum RequestCountType {
        EXACTLY,
        LESS_THAN,
        GREATER_THAN,
        LESS_THAN_OR_EQUAL,
        GREATER_THAN_OR_EQUAL
    }

    private final RequestCountType requestCountType;
    private final int value;

    public RequestCount(RequestCountType requestCountType, int value) {
        this.requestCountType = requestCountType;
        this.value = value;
    }

    public static RequestCount exactly(int value) {
        return new RequestCount(RequestCountType.EXACTLY, value);
    }

    public static RequestCount lessThan(int value) {
        return new RequestCount(RequestCountType.LESS_THAN, value);
    }

    public static RequestCount greaterThan(int value) {
        return new RequestCount(RequestCountType.GREATER_THAN, value);
    }

    public static RequestCount lessThanOrEqualTo(int value) {
        return new RequestCount(RequestCountType.LESS_THAN_OR_EQUAL, value);
    }

    public static RequestCount greaterThanOrEqualTo(int value) {
        return new RequestCount(RequestCountType.GREATER_THAN_OR_EQUAL, value);
    }

    public RequestCountType getRequestCountType() {
        return requestCountType;
    }

    public int getValue() {
        return value;
    }

}
