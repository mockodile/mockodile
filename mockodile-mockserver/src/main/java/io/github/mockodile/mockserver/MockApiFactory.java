package io.github.mockodile.mockserver;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.mockodile.MockApi;
import io.github.mockodile.Mocker;
import io.github.mockodile.ObjectMapper;
import org.mockserver.client.MockServerClient;

public final class MockApiFactory {

    private MockApiFactory() {
        // do not construct
    }

    public static MockApi from(MockServerClient mockServerClient) {
        return from(() -> mockServerClient);
    }

    public static MockApi from(MockServerClientProvider mockServerClientProvider) {
        // TODO allow clients to specify ObjectMapper and registered modules etc.
        com.fasterxml.jackson.databind.ObjectMapper delegate = new com.fasterxml.jackson.databind.ObjectMapper();
        delegate.registerModule(new JavaTimeModule());
        ObjectMapper objectMapper = new ObjectMapper(delegate);
        Mocker mocker = new MockServerMocker(mockServerClientProvider, objectMapper);
        return new MockApi(mocker);
    }

}
