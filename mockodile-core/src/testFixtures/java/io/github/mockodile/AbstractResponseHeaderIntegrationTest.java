package io.github.mockodile;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.RequestMapping;
import io.github.mockodile.annotations.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractResponseHeaderIntegrationTest extends AbstractIntegrationTest {

    @Override
    void initMocks(MockApi mockApi) {
        // no need to initialize any clients for this test
    }

    @Test
    void mockGetForResourceWithHeadersOnTypeAndMethod_requestResource_specifiedResponseReturned() {
        @MockedApi
        @RequestMapping(path = "/root-path", responseHeaders = "x-header-mapping-1=h1")
        interface TestMockedApi {

            @Get(path = "/say-hello")
            @Response(headers = "x-header-mapping-2=h2")
            String sayHello();
        }
        var testMockedApi = mockApi.mock(TestMockedApi.class);

        mockApi.when(testMockedApi.sayHello()).thenReturn("hello");

        HttpResponse<String> response = getWithFixedEndpoint();

        assertThat(response.headers().map())
                .containsEntry("x-header-mapping-1", List.of("h1"))
                .containsEntry("x-header-mapping-2", List.of("h2"));
    }

    @Test
    void mockGetForResourceWithHeadersOnTypeOnly_requestResource_specifiedResponseReturned() {
        @MockedApi
        @RequestMapping(path = "/root-path", responseHeaders = "x-header-mapping-1=h1")
        interface TestMockedApi {

            @Get(path = "/say-hello")
            @Response(headers = "x-header-mapping-2=h2")
            String sayHello();
        }

        var testMockedApi = mockApi.mock(TestMockedApi.class);
        mockApi.when(testMockedApi.sayHello()).thenReturn("hello");

        HttpResponse<String> response = getWithFixedEndpoint();

        assertThat(response.headers().map())
                .containsEntry("x-header-mapping-1", List.of("h1"));
    }

    @Test
    void mockGetForResourceWithHeadersOnMethodOnly_requestResource_specifiedResponseReturned() {
        @MockedApi
        @RequestMapping(path = "/root-path")
        interface TestMockedApi {

            @Get(path = "/say-hello")
            @Response(headers = "x-header-mapping-2=h2")
            String sayHello();
        }

        var testMockedApi = mockApi.mock(TestMockedApi.class);
        mockApi.when(testMockedApi.sayHello()).thenReturn("hello");

        HttpResponse<String> response = getWithFixedEndpoint();

        assertThat(response.headers().map())
                .containsEntry("x-header-mapping-2", List.of("h2"));
    }

    @Test
    void mockGetForResourceWithHeaderOverwriteOnMethod_requestResource_specifiedResponseReturned() {
        @MockedApi
        @RequestMapping(path = "/root-path", responseHeaders = "x-header-overwrite=should-not-be-present")
        interface TestMockedApi {

            @Get(path = "/say-hello")
            @Response(headers = "x-header-overwrite=overwritten-value")
            String sayHello();
        }

        var testMockedApi = mockApi.mock(TestMockedApi.class);
        mockApi.when(testMockedApi.sayHello()).thenReturn("hello");

        HttpResponse<String> response = getWithFixedEndpoint();

        assertThat(response.headers().map())
                .containsEntry("x-header-overwrite", List.of("overwritten-value"));
    }

    private HttpResponse<String> getWithFixedEndpoint() {
        try {
            return HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/say-hello"))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
