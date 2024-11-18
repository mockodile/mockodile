package io.github.mockodile;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.HeaderParam;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.RequestMapping;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

import static io.github.mockodile.ArgumentMatchers.matchesAnyString;
import static org.assertj.core.api.Assertions.*;

public abstract class AbstractHeaderParameterIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {
        @Get(path = "/header-param")
        String getWithHeaderParam(@HeaderParam String h2);
    }

    @Test
    void mockGetWithHeaderParam_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        String headerParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithHeaderParam(headerParamValue)).thenReturn(helloMessage);

        HttpResponse<String> response = getWithHeaderParam(headerParamValue);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetWithHeaderParam_requestResourceWithMismatchedHeaderValue_notFound() {
        String helloMessage = "hello " + UUID.randomUUID();
        String headerParamValue = UUID.randomUUID().toString();
        String wrongHeaderParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithHeaderParam(headerParamValue)).thenReturn(helloMessage);

        HttpResponse<String> response = getWithHeaderParam(wrongHeaderParamValue);

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void mockGetWithHeaderParamAnyValue_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        String headerParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithHeaderParam(matchesAnyString())).thenReturn(helloMessage);

        HttpResponse<String> response = getWithHeaderParam(headerParamValue);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetWithHeaderParamAnyValue_requestResourceWithoutHeaderParam_notFound() {
        String helloMessage = "hello " + UUID.randomUUID();
        mockApi.when(testMockedApi.getWithHeaderParam(matchesAnyString())).thenReturn(helloMessage);

        HttpResponse<String> response = getWithHeaderParam(null);

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void mockGetWithHeaderParamAndRequestResource_verify_assertionPasses() {
        String headerParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithHeaderParam(headerParamValue))
                .thenReturn("hello " + UUID.randomUUID());
        getWithHeaderParam(headerParamValue);

        assertThatNoException().isThrownBy(() -> mockApi.verify(testMockedApi.getWithHeaderParam(headerParamValue)));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void mockGetWithHeaderParamAndRequestResource_verifyWithMismatchedHeaderValue_assertionPasses() {
        String headerParamValue = UUID.randomUUID().toString();
        String wrongHeaderParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithHeaderParam(headerParamValue))
                .thenReturn("hello " + UUID.randomUUID());
        getWithHeaderParam(headerParamValue);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(testMockedApi.getWithHeaderParam(wrongHeaderParamValue)));
    }

    @Test
    void mockGetWithHeaderParamAndRequestResource_verifyWithAnyValue_assertionPasses() {
        String headerParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithHeaderParam(headerParamValue))
                .thenReturn("hello " + UUID.randomUUID());
        getWithHeaderParam(headerParamValue);

        assertThatNoException()
                .isThrownBy(() -> mockApi.verify(testMockedApi.getWithHeaderParam(matchesAnyString())));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void mockGetWithHeaderParamAndRequestResource_verifyNone_assertionError() {
        String headerParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithHeaderParam(headerParamValue))
                .thenReturn("hello " + UUID.randomUUID());
        getWithHeaderParam(headerParamValue);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getWithHeaderParam(matchesAnyString())));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void mockGetWithHeaderParamEqualsAndRequestResourceNotMatched_verifyNone_assertionPasses() {
        mockApi.when(testMockedApi.getWithHeaderParam("hello.*world"))
                .thenReturn("hello " + UUID.randomUUID());
        getWithHeaderParam("hello-world");

        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getWithHeaderParam("hello.*world")));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void none_verifyWithAnyValue_assertionError() {
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(testMockedApi.getWithHeaderParam(matchesAnyString())));
    }

    @Test
    void none_verifyNone_assertionPasses() {
        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getWithHeaderParam(matchesAnyString())));
    }

    private HttpResponse<String> getWithHeaderParam(String headerParamValue) {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(TestUtil.uri(mockUrl, "/root-path/header-param"))
                .GET();

        if (headerParamValue != null) {
            requestBuilder.header("h2", headerParamValue);
        }

        try {
            return HttpClient.newHttpClient()
                    .send(
                            requestBuilder.build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
