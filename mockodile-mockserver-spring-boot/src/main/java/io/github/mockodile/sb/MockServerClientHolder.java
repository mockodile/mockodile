package io.github.mockodile.sb;

import io.github.mockodile.mockserver.MockServerClientProvider;
import org.mockserver.client.MockServerClient;

public class MockServerClientHolder implements MockServerClientProvider {

    private MockServerClient mockServerClient;

    public MockServerClient getMockServerClient() {
        return mockServerClient;
    }

    void setMockServerClient(MockServerClient mockServerClient) {
        this.mockServerClient = mockServerClient;
    }
}
