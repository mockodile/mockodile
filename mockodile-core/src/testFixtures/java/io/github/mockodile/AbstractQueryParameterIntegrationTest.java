package io.github.mockodile;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.QueryParam;
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

public abstract class AbstractQueryParameterIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {
        @Get(path = "/query-param")
        String getWithQueryParam(@QueryParam String q1);
    }

    @Test
    void mockGetWithQueryParam_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        String queryParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithQueryParam(queryParamValue)).thenReturn(helloMessage);

        HttpResponse<String> response = getWithQueryParam(queryParamValue);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetWithQueryParam_requestResourceWithMismatchedQueryParamValue_notFound() {
        String helloMessage = "hello " + UUID.randomUUID();
        String queryParamValue = UUID.randomUUID().toString();
        String wrongQueryParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithQueryParam(queryParamValue)).thenReturn(helloMessage);

        HttpResponse<String> response = getWithQueryParam(wrongQueryParamValue);

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void mockGetWithQueryParamMatchesAnyValue_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        String queryParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithQueryParam(matchesAnyString())).thenReturn(helloMessage);

        HttpResponse<String> response = getWithQueryParam(queryParamValue);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetWithQueryParam_requestResourceWithoutQueryParam_notFound() {
        String helloMessage = "hello " + UUID.randomUUID();
        String queryParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithQueryParam(queryParamValue)).thenReturn(helloMessage);

        HttpResponse<String> response = getWithQueryParam(null);

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void mockGetWithQueryParamAndRequestResource_verify_assertionPasses() {
        String queryParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithQueryParam(matchesAnyString())).thenReturn("hello " + UUID.randomUUID());
        getWithQueryParam(queryParamValue);

        assertThatNoException()
                .isThrownBy(() -> mockApi.verify(testMockedApi.getWithQueryParam(queryParamValue)));
    }

    @Test
    void mockGetWithQueryParamAndRequestResource_verifyMatchesAnyString_assertionPasses() {
        String queryParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithQueryParam(matchesAnyString())).thenReturn("hello " + UUID.randomUUID());
        getWithQueryParam(queryParamValue);

        assertThatNoException()
                .isThrownBy(() -> mockApi.verify(testMockedApi.getWithQueryParam(matchesAnyString())));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void mockGetWithQueryParamAndRequestResource_verifyNone_assertionError() {
        String queryParamValue = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getWithQueryParam(matchesAnyString())).thenReturn("hello " + UUID.randomUUID());
        getWithQueryParam(queryParamValue);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getWithQueryParam(queryParamValue)));
    }

    @Test
    void mockGetAndRequestResourceWithQueryParamEqualsNotMatched_verifyNone_assertionPasses() {
        mockApi.when(testMockedApi.getWithQueryParam("any.*id")).thenReturn("hello " + UUID.randomUUID());
        // does not match with an equals
        getWithQueryParam("any-id");

        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getWithQueryParam("any.*id")));
    }


    @Test
    void none_verifyNone_assertionPasses() {
        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getWithQueryParam(matchesAnyString())));
    }

    private HttpResponse<String> getWithQueryParam(String queryParamValue) {
        var queryParamString = queryParamValue == null ? "" : "?q1=" + queryParamValue;

        try {
            return HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/query-param" + queryParamString))
                                    .GET().build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
