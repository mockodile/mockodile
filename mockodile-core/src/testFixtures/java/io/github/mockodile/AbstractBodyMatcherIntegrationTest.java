package io.github.mockodile;

import io.github.mockodile.annotations.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static io.github.mockodile.ArgumentMatchers.*;
import static io.github.mockodile.TestUtil.uri;
import static org.assertj.core.api.Assertions.*;

public abstract class AbstractBodyMatcherIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {
        @Response(status = 201)
        @Post(path = "/create")
        void create(@RequestBody TestBody body);
    }

    record TestBody(String name, String house) {
        String toJson() {
            return """
                    {"name":"%s","house":"%s"}""".formatted(name, house);
        }
    }

    @Test
    void mockPostWithRequestBodyMatchesAny_requestResource_specifiedResponseReturned() {
        mockApi.when(() -> testMockedApi.create(anyBody())).willReturn();

        HttpResponse<String> response = postCreate(new TestBody("Harry", "Gryffindor"));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).isEmpty();
    }

    @Test
    void mockPostWithRequestBodyEquals_requestResource_specifiedResponseReturned() {
        var testBody = new TestBody("Draco", "Slytherin");
        mockApi.when(() -> testMockedApi.create(testBody)).willReturn();

        // comparing bodies as formatted strings is probably not very useful
        // however it is supported therefore it is tested here
        HttpResponse<String> response = postCreate(testBody);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).isEmpty();
    }

    @Test
    void mockPostWithRequestMatchesExpression_requestResource_specifiedResponseReturned() {
        var testBody = new TestBody("Luna", "Ravenclaw");
        mockApi.when(() -> testMockedApi.create(matches(".*Luna.*"))).willReturn();

        // comparing bodies using patterns is probably not very useful
        // however it is supported therefore it is tested here
        HttpResponse<String> response = postCreate(testBody);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).isEmpty();
    }

    @Test
    void mockPostWithRequestMatchesExpression_requestResourceWithMismatchedBody_notFound() {
        var testBody = new TestBody("Luna", "Ravenclaw");
        mockApi.when(() -> testMockedApi.create(matches(".*Neville.*"))).willReturn();

        // comparing bodies using patterns is probably not very useful
        // however it is supported therefore it is tested here
        HttpResponse<String> response = postCreate(testBody);

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void mockPostAndRequestResource_verifyAnyBody_assertionPasses() {
        var testBody = new TestBody("Luna", "Ravenclaw");
        mockApi.when(() -> testMockedApi.create(anyBody())).willReturn();
        postCreate(testBody);

        assertThatNoException().isThrownBy(() -> mockApi.verify(() -> testMockedApi.create(anyBody())));
    }

    @Test
    void mockPostAndRequestResource_verifyBodyEquals_assertionPasses() {
        var testBody = new TestBody("Luna", "Ravenclaw");
        mockApi.when(() -> testMockedApi.create(anyBody())).willReturn();
        postCreate(testBody);

        assertThatNoException().isThrownBy(() -> mockApi.verify(() -> testMockedApi.create(testBody)));
    }

    @Test
    void mockPostAndRequestResource_verifyBodyEqualsWithMismatchedBody_assertionError() {
        var testBody = new TestBody("Luna", "Ravenclaw");
        var testBodyDoesNotMatch = new TestBody("Luma", "Ravenclam");
        mockApi.when(() -> testMockedApi.create(anyBody())).willReturn();
        postCreate(testBody);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(() -> testMockedApi.create(testBodyDoesNotMatch)));
    }

    @Test
    void mockPostAndRequestResource_verifyBodyEqualsExpressionMatch_assertionPasses() {
        var testBody = new TestBody("Luna", "Ravenclaw");
        mockApi.when(() -> testMockedApi.create(anyBody())).willReturn();
        postCreate(testBody);

        assertThatNoException()
                .isThrownBy(() -> mockApi.verify(() -> testMockedApi.create(matches(".*Luna.*"))));
    }

    @Test
    void mockPostAndRequestResource_verifyBodyEqualsExpressionMatchWhichDoesNotMatch_assertionError() {
        var testBody = new TestBody("Luna", "Ravenclaw");
        mockApi.when(() -> testMockedApi.create(anyBody())).willReturn();
        postCreate(testBody);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(() -> testMockedApi.create(matches(".*Luma.*"))));
    }

    @Test
    void mockPostAndRequestResource_verifyNone_assertionError() {
        var testBody = new TestBody("Luna", "Ravenclaw");
        mockApi.when(() -> testMockedApi.create(anyBody())).willReturn();
        postCreate(testBody);

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verifyNone(() -> testMockedApi.create(matchesAnyString())));
    }

    @Test
    void none_verifyAnyBody_assertionError() {
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> mockApi.verify(() -> testMockedApi.create(anyBody())));
    }

    @Test
    void none_verifyNone_assertionPasses() {
        assertThatNoException()
                .isThrownBy(() -> mockApi.verifyNone(() -> testMockedApi.create(anyBody())));
    }

    private HttpResponse<String> postCreate(TestBody testBody) {
        try {
            return HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(uri(mockUrl, "/root-path/create"))
                                    .POST(HttpRequest.BodyPublishers.ofString(testBody.toJson()))
                                    .build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
