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

public abstract class AbstractStaticHeaderIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {

        @Get(path = "/requires-header", headers = "h1=v1")
        String getWithHeader();
    }

    @Test
    void mockGetWithHeaderAnnotation_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        mockApi.when(testMockedApi.getWithHeader()).willReturn(helloMessage);

        String headerValue = "v1";
        HttpResponse<String> response = getWithHeader(headerValue);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetWithHeaderAnnotation_requestResourceWithHeaderValueMismatch_notFound() {
        String helloMessage = "hello " + UUID.randomUUID();
        mockApi.when(testMockedApi.getWithHeader()).willReturn(helloMessage);

        String headerValue = "v2";
        HttpResponse<String> response = getWithHeader(headerValue);

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void mockGetWithHeaderAnnotationAndRequestResource_verify_assertionPasses() {
        mockApi.when(testMockedApi.getWithHeader()).willReturn("hello " + UUID.randomUUID());
        getWithHeader("v1");

        assertThatNoException()
                .isThrownBy(() -> mockApi.verify(testMockedApi.getWithHeader()));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void mockGetWithHeaderAnnotationAndRequestResource_verifyNone_assertionError() {
        mockApi.when(testMockedApi.getWithHeader()).willReturn("hello " + UUID.randomUUID());
        getWithHeader("v1");

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getWithHeader()));
    }

    @Test
    void none_verifyNone_assertionPasses() {
        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getWithHeader()));
    }

    private HttpResponse<String> getWithHeader(String headerValue) {
        try {
            return HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/requires-header"))
                                    .header("h1", headerValue)
                                    .GET()
                                    .build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
