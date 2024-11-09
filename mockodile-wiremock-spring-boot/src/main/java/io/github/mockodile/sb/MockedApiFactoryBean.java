package io.github.mockodile.sb;

import io.github.mockodile.MockApi;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.NonNull;

class MockedApiFactoryBean implements FactoryBean<Object> {

    private final Class<?> type;
    private final MockApi mockApi;

    MockedApiFactoryBean(@NonNull Class<?> type, @NonNull MockApi mockApi) {
        this.type = type;
        this.mockApi = mockApi;
    }

    @Override
    public Object getObject() {
        return mockApi.mock(type);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }
}
