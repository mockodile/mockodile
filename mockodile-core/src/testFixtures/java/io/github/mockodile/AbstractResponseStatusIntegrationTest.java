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

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractResponseStatusIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        this.testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {
        @Get(path = "/say-im-a-teapot")
        @Response(status = 418)
        String imATeapot();

        @Get(path = "/say-ok")
        String ok();
    }

    @Test
    void mockWithCustomResponseStatus_requestResource_specifiedResponseReturned() {
        mockApi.when(testMockedApi.imATeapot())
                .willReturn("short and stout");

        var response = getSayImATeapot();

        assertThat(response.statusCode()).isEqualTo(418);
    }

    @Test
    void mockWithDefaultResponseStatus_requestResource_specifiedResponseReturned() {
        mockApi.when(testMockedApi.ok())
                .willReturn("here is my handle");

        var response = getSayOk();
        assertThat(response.statusCode()).isEqualTo(200);
    }

    private HttpResponse<String> getSayImATeapot() {
        return get("say-im-a-teapot");
    }

    private HttpResponse<String> getSayOk() {
        return get("say-ok");
    }

    private HttpResponse<String> get(String path) {
        try {
            return HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/" + path))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
