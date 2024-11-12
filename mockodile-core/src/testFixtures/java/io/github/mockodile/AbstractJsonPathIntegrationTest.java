package io.github.mockodile;

import io.github.mockodile.annotations.JsonPath;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.Post;
import io.github.mockodile.annotations.RequestMapping;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static io.github.mockodile.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;

public abstract class AbstractJsonPathIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {
        @Post(path = "/create")
        void createMatchWithJsonPath(@JsonPath(expression = "$.name") String name, @JsonPath(expression = "$.house") String house);

        @Post(path = "/createWithBoolean")
        void createMatchWithJsonPathBoolean(@JsonPath(expression = "$.name") String name, @JsonPath(expression = "$.flag") boolean flag);

        @Post(path = "/createWithNumber")
        void createMatchWithJsonPathNumber(@JsonPath(expression = "$.name") String name, @JsonPath(expression = "$.number") int number);
    }

    record TestBody(String name, String house) {
        String toJson() {
            return """
                    {"name":"%s","house":"%s"}""".formatted(name, house);
        }
    }

    record TestBodyWithFlag(String name, boolean flag) {
        String toJson() {
            return """
                    {"name":"%s","flag":%b}""".formatted(name, flag);
        }
    }

    record TestBodyWithNumber(String name, int number) {
        String toJson() {
            return """
                    {"name":"%s","number":%s}""".formatted(name, number);
        }
    }

    @Test
    void mockPostWithJsonPath_requestResource_specifiedResponseReturned() {
        var testBody = new TestBody("Cedric", "Hufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath("Cedric", "Hufflepuff")).willReturn();

        HttpResponse<String> response = postToCreateEndpoint(testBody);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEmpty();
    }

    @Test
    void mockPostWithJsonPath_requestResourceWithMismatch_specifiedResponseReturned() {
        var testBody = new TestBody("Cedric", "Hufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath("Cedric", "Gryffindor")).willReturn();

        HttpResponse<String> response = postToCreateEndpoint(testBody);

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void mockPostWithJsonPathMatches_requestResource_specifiedResponseReturned() {
        var testBody = new TestBody("Cedric", "Hufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath(eq("Cedric"), matches("[hH]ufflepuff"))).willReturn();

        HttpResponse<String> response = postToCreateEndpoint(testBody);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEmpty();
    }

    @Test
    void mockPostWithJsonPathMatches_requestResourceWithMismatchedBody_specifiedResponseReturned() {
        var testBody = new TestBody("Cedric", "Pufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath(eq("Cedric"), matches("[hH]ufflepuff"))).willReturn();

        HttpResponse<String> response = postToCreateEndpoint(testBody);

        assertThat(response.statusCode()).isEqualTo(404);
    }
    
    @Test
    void mockPostWithJsonPathAndRequestResource_verify_assertionPasses() {
        var testBody = new TestBody("Cedric", "Hufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath(matchesAnyString(), matchesAnyString())).willReturn();

        postToCreateEndpoint(testBody);

        assertThatNoException().isThrownBy(() -> mockApi.verify(() -> testMockedApi.createMatchWithJsonPath("Cedric", "Hufflepuff")));
    }

    @Test
    void mockPostWithJsonPathAndRequestResource_verifyWithMismatchedValue_assertionError() {
        var testBody = new TestBody("Cedric", "Hufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath(matchesAnyString(), matchesAnyString())).willReturn();

        postToCreateEndpoint(testBody);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(() -> testMockedApi.createMatchWithJsonPath("Cedrik", "Hupplepuff")));
    }

    @Test
    void mockPostWithJsonPathAndRequestResource_verifyWithPatternMatcher_assertionPasses() {
        var testBody = new TestBody("Cedric", "Hufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath(matchesAnyString(), matchesAnyString())).willReturn();

        postToCreateEndpoint(testBody);

        assertThatNoException()
                .isThrownBy(() -> mockApi.verify(() -> testMockedApi.createMatchWithJsonPath(matches("[Cc]edri[ck]"), matches(".*lepuf.*"))));
    }

    @Test
    void mockPostWithJsonPathAndRequestResource_verifyWithPatternMatcherDoesntMatch_assertionError() {
        var testBody = new TestBody("Cedric", "Hufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath(matchesAnyString(), matchesAnyString())).willReturn();

        postToCreateEndpoint(testBody);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(() -> testMockedApi.createMatchWithJsonPath(matches("[Cc]eDri[ck]"), matches(".*lepup.*"))));
    }

    @Test
    void mockPostWithJsonPathAndRequestResource_verifyNone_assertionError() {
        var testBody = new TestBody("Cedric", "Hufflepuff");
        mockApi.when(() -> testMockedApi.createMatchWithJsonPath(matchesAnyString(), matchesAnyString())).willReturn();

        postToCreateEndpoint(testBody);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verifyNone(() -> testMockedApi.createMatchWithJsonPath(matchesAnyString(), matchesAnyString())));
    }

    @Test
    void none_verifyWithJsonPathMatchesAnyString_assertionError() {
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(() -> testMockedApi.createMatchWithJsonPath(matchesAnyString(), matchesAnyString())));
    }

    @Test
    void none_verifyNone_assertionPasses() {
        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(() -> testMockedApi.createMatchWithJsonPath(matchesAnyString(), matchesAnyString())));
    }

    @Test
    void mockPostWithJsonPathWithBooleanAndRequestResource_verify_passes() {
        var testBody = new TestBodyWithFlag("Luna", true);
        mockApi.when(() -> testMockedApi.createMatchWithJsonPathBoolean("Luna", true)).willReturn();

        try {
            HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/createWithBoolean"))
                                    .POST(HttpRequest.BodyPublishers.ofString(testBody.toJson()))
                                    .build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        mockApi.verify(() -> testMockedApi.createMatchWithJsonPathBoolean("Luna", true));
    }

    @Test
    void mockPostWithJsonPathWithNumberAndRequestResource_verify_passes() {
        var testBody = new TestBodyWithNumber("Ginny", 42);
        mockApi.when(() -> testMockedApi.createMatchWithJsonPathNumber("Ginny", 42)).willReturn();

        try {
            HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/createWithNumber"))
                                    .POST(HttpRequest.BodyPublishers.ofString(testBody.toJson()))
                                    .build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        mockApi.verify(() -> testMockedApi.createMatchWithJsonPathNumber("Ginny", 42));
    }

    private HttpResponse<String> postToCreateEndpoint(TestBody testBody) {
        try {
            return HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/create"))
                                    .POST(HttpRequest.BodyPublishers.ofString(testBody.toJson()))
                                    .build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
