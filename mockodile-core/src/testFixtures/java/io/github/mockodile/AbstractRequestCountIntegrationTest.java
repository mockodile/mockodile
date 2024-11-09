package io.github.mockodile;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.RequestMapping;
import io.github.mockodile.domain.RequestCount;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

public abstract class AbstractRequestCountIntegrationTest extends AbstractIntegrationTest {

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
    void invokeOne_verifyOne_assertionPasses() {
        mockApi.when(testMockedApi.sayHello()).willReturn("hello");
        getWithFixedEndpoint();

        assertThatNoException().isThrownBy(() -> mockApi.verify(1, testMockedApi.sayHello()));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void invokeOnce_verifyNone_assertionError() {
        mockApi.when(testMockedApi.sayHello()).willReturn("hello");
        getWithFixedEndpoint();

        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> mockApi.verifyNone(testMockedApi.sayHello()));
    }

    @Test
    void invokeOnce_verifyLessThanTwo_assertionPasses() {
        mockApi.when(testMockedApi.sayHello()).willReturn("hello");
        getWithFixedEndpoint();

        assertThatNoException().isThrownBy(() -> mockApi.verify(RequestCount.lessThan(2), testMockedApi.sayHello()));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void invokeOnce_verifyLessThanOne_assertionError() {
        mockApi.when(testMockedApi.sayHello()).willReturn("hello");
        getWithFixedEndpoint();

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(RequestCount.lessThan(1), testMockedApi.sayHello()));
    }

    @Test
    void invokeTwice_verifyTwo_assertionPasses() {
        mockApi.when(testMockedApi.sayHello()).willReturn("hello");
        getWithFixedEndpoint();
        getWithFixedEndpoint();

        assertThatNoException().isThrownBy(() -> mockApi.verify(2, testMockedApi.sayHello()));
    }

    @Test
    void invokeTwice_verifyMoreThanOne_assertionPasses() {
        mockApi.when(testMockedApi.sayHello()).willReturn("hello");
        getWithFixedEndpoint();
        getWithFixedEndpoint();

        assertThatNoException().isThrownBy(() -> mockApi.verify(RequestCount.greaterThan(1), testMockedApi.sayHello()));
    }

    @Test
    void invokeZero_verifyZero_assertionPasses() {
        assertThatNoException().isThrownBy(() -> mockApi.verify(0, testMockedApi.sayHello()));
    }

    @Test
    void invokeZero_verifyLessThanTwo_assertionPasses() {
        assertThatNoException().isThrownBy(() -> mockApi.verify(RequestCount.lessThan(2), testMockedApi.sayHello()));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void invokeZero_verifyOne_assertionError() {
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> mockApi.verify(1, testMockedApi.sayHello()));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void invokeTwice_verifyMoreThanTwo_assertionError() {
        mockApi.when(testMockedApi.sayHello()).willReturn("hello");
        getWithFixedEndpoint();
        getWithFixedEndpoint();

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(RequestCount.greaterThan(2), testMockedApi.sayHello()));
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
