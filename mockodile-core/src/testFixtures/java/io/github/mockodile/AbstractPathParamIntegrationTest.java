package io.github.mockodile;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.PathParam;
import io.github.mockodile.annotations.RequestMapping;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Random;
import java.util.UUID;

import static io.github.mockodile.ArgumentMatchers.anyPath;
import static io.github.mockodile.ArgumentMatchers.pathMatches;
import static org.assertj.core.api.Assertions.*;

public abstract class AbstractPathParamIntegrationTest extends AbstractIntegrationTest {

    protected TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    public interface TestMockedApi {
        @Get(path = "/id/{id}")
        String getById(@PathParam String id);
    }

    @Test
    protected void mockGetWithPathParam_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        String id = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getById(id)).thenReturn(helloMessage);

        HttpResponse<String> response = getWithPathParam(id);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetWithPathParam_requestResourceWithWrongId_notFound() {
        String helloMessage = "hello " + UUID.randomUUID();
        String id = UUID.randomUUID().toString();
        String wrongId = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getById(id)).thenReturn(helloMessage);

        HttpResponse<String> response = getWithPathParam(wrongId);

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void mockGetWithPathParamMatchingAnyValue_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        String id = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getById(anyPath())).thenReturn(helloMessage);

        HttpResponse<String> response = getWithPathParam(id);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetWithPathParamMatchingRegularExpression_requestResource_specifiedResponseReturned() {
        String helloMessage = "hello " + UUID.randomUUID();
        var random = new Random();
        String id = Math.abs(random.nextInt(31)) + "-separator-" + Math.abs(random.nextInt(31));

        mockApi.when(testMockedApi.getById(pathMatches("[0-9]*-separator-[0-9]*"))).thenReturn(helloMessage);

        HttpResponse<String> response = getWithPathParam(id);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("\"" + helloMessage + "\"");
    }

    @Test
    void mockGetAndRequestResource_verifyWithPathParam_assertionPasses() {
        String id = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getById(anyPath())).thenReturn("hello " + UUID.randomUUID());
        getWithPathParam(id);

        assertThatNoException().isThrownBy(() -> mockApi.verify(testMockedApi.getById(id)));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void mockGetAndRequestResource_verifyWithMismatchedPathParamValue_assertionError() {
        String id = UUID.randomUUID().toString();
        String mismatchedId = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getById(anyPath())).thenReturn("hello " + UUID.randomUUID());
        getWithPathParam(id);

        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> mockApi.verify(testMockedApi.getById(mismatchedId)));
    }

    @Test
    void mockGetAndRequestResource_verifyWithAnyPathParamValue_assertionPasses() {
        String id = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getById(anyPath())).thenReturn("hello " + UUID.randomUUID());
        getWithPathParam(id);

        assertThatNoException().isThrownBy(() -> mockApi.verify(testMockedApi.getById(anyPath())));
    }

    @Test
    void mockGetAndRequestResource_verifyWithCustomPattern_assertionPasses() {
        Random random = new Random();
        String id = Math.abs(random.nextInt(31)) + "-separator-" + Math.abs(random.nextInt(31));
        mockApi.when(testMockedApi.getById(anyPath())).thenReturn("hello " + UUID.randomUUID());
        getWithPathParam(id);

        assertThatNoException()
                .isThrownBy(() -> mockApi.verify(testMockedApi.getById(pathMatches("[0-9]*-separator-[0-9]*"))));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void mockGetAndRequestResource_verifyWithMismatchedCustomPattern_assertionError() {
        Random random = new Random();
        String id = Math.abs(random.nextInt(31)) + "-separator-" + Math.abs(random.nextInt(31));
        mockApi.when(testMockedApi.getById(anyPath())).thenReturn("hello " + UUID.randomUUID());
        getWithPathParam(id);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(testMockedApi.getById(pathMatches("[0-9]*-mismatch-[0-9]*"))));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void mockGetAndRequestResource_verifyNone_assertionError() {
        String id = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getById(anyPath())).thenReturn("hello " + UUID.randomUUID());
        getWithPathParam(id);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getById(anyPath())));
    }

    @Test
    void mockGetAndRequestResource_verifyNoneWithDifferentId_assertionPasses() {
        String id = UUID.randomUUID().toString();
        String notCalled = UUID.randomUUID().toString();
        mockApi.when(testMockedApi.getById(anyPath())).thenReturn("hello " + UUID.randomUUID());
        getWithPathParam(id);

        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getById(notCalled)));
    }

    @Test
    void mockGetAndRequestResourceWithEqualsNotMatched_verifyNone_assertionPasses() {
        mockApi.when(testMockedApi.getById("any.*id")).thenReturn("hello " + UUID.randomUUID());
        // does not match with an equals
        getWithPathParam("any-id");

        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(testMockedApi.getById("any.*id")));
    }

    protected HttpResponse<String> getWithPathParam(String id) {
        try {
            return HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/id/" + id))
                                    .GET()
                                    .build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
