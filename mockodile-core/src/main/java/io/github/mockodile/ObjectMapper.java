package io.github.mockodile;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public class ObjectMapper {

    private final com.fasterxml.jackson.databind.ObjectMapper delegate;

    public ObjectMapper(com.fasterxml.jackson.databind.ObjectMapper delegate) {
        this.delegate = delegate;
    }

    // useful for testing
    static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper(new com.fasterxml.jackson.databind.ObjectMapper());
    }

    public String writeValueAsString(Object value) {
        try {
            return delegate.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ObjectMapperWrapperRuntimeException(e);
        }
    }

    public byte[] writeValueAsBytes(Object value) {
        try {
            return delegate.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new ObjectMapperWrapperRuntimeException(e);
        }
    }

    public <T> T readValue(String s, Class<T> type) {
        try {
            return delegate.readValue(s, type);
        } catch (JsonProcessingException e) {
            throw new ObjectMapperWrapperRuntimeException(e);
        }
    }

    public <T> T readValue(byte[] body, Class<T> type) {
        try {
            return delegate.readValue(body, type);
        } catch (IOException e) {
            throw new ObjectMapperWrapperRuntimeException(e);
        }
    }

    public static class ObjectMapperWrapperRuntimeException extends RuntimeException {
        public ObjectMapperWrapperRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
