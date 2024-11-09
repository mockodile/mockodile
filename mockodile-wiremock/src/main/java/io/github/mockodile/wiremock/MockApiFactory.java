package io.github.mockodile.wiremock;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.mockodile.MockApi;
import io.github.mockodile.Mocker;
import io.github.mockodile.ObjectMapper;

public final class MockApiFactory {

    private MockApiFactory() {
        // do not construct
    }

    /**
     * Construct a MockApi with the provided WireMockServer.
     * This factory method is used for environments where the WireMockServer is available
     * @param wireMockServer the wire mock server
     */
    public static MockApi from(WireMockServer wireMockServer) {
        return from(new WireMock(wireMockServer));
    }

    /**
     * Construct a MockApi with the provided WireMock.
     * This factory method is used for environments where the WireMock object is available, for example for
     * test cases which use WireMockExtension for junit5.
     * @param wireMock the wire mock object
     */
    public static MockApi from(WireMock wireMock) {
        // TODO allow clients to specify ObjectMapper and registered modules etc.
        com.fasterxml.jackson.databind.ObjectMapper delegate = new com.fasterxml.jackson.databind.ObjectMapper();
        delegate.registerModule(new JavaTimeModule());
        ObjectMapper objectMapper = new ObjectMapper(delegate);
        Mocker mocker = new WireMockMocker(wireMock, objectMapper);
        return new MockApi(mocker);
    }
}
