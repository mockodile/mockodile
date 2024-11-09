package io.github.mockodile.mockserver;

import org.mockserver.client.MockServerClient;

public interface MockServerClientProvider {
    MockServerClient getMockServerClient();
}
