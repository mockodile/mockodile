package io.github.mockodile;

import org.junit.jupiter.api.BeforeEach;

abstract class AbstractIntegrationTest {

    protected MockApi mockApi;
    protected String mockUrl;

    // mockApiProvider must be provided by an extension in a subclass
    @BeforeEach
    protected void setUp(MockApiProvider mockApiProvider) {
        this.mockApi = mockApiProvider.mockApi();
        mockApi.reset();
        initMocks(mockApi);
        mockUrl = "http://localhost:" + mockApiProvider.port();
    }

    abstract void initMocks(MockApi mockApi);
}
