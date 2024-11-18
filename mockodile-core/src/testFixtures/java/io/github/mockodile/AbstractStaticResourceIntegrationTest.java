package io.github.mockodile;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.RequestMapping;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public abstract class AbstractStaticResourceIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {
        @Get(path = "/say-hello")
        String sayHello();
    }

    @Test
    void mockGetForStaticResource_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        mockApi.when(testMockedApi.sayHello())
                .thenReturn(helloMessage);

        HttpResponse<String> response = getWithFixedEndpoint();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetForStaticResourceAndRequestResource_verifyEndpointCalled_assertionPasses() {
        String helloMessage = "hello " + UUID.randomUUID();
        mockApi.when(testMockedApi.sayHello())
                .thenReturn(helloMessage);
        getWithFixedEndpoint();

        mockApi.verify(testMockedApi.sayHello());
    }

    @Test
    @SuppressWarnings("java:S5778")
    void none_verifyEndpointCalled_assertionError() {
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(testMockedApi.sayHello()));
    }

    @Test
    void none_verifyNone_assertionPasses() {
        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.sayHello()));
    }

    private HttpResponse<String> getWithFixedEndpoint() {
        try {
            return HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/say-hello"))
                                    .GET()
                                    .build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
