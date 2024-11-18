package io.github.mockodile;

import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.Post;
import io.github.mockodile.annotations.RequestBody;
import io.github.mockodile.annotations.RequestMapping;
import io.github.mockodile.domain.BodyAsString;
import io.github.mockodile.domain.HttpHeaders;
import io.github.mockodile.domain.QueryParams;
import io.github.mockodile.domain.Request;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import static io.github.mockodile.ArgumentMatchers.anyBody;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractRequestVerificationIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {

        @Post(path = "/create")
        void create(@RequestBody TestDto testDto);
    }

    record TestDto(String firstName, String lastname, int age) {
        String toJson() {
            return """
                    {"firstName":"%s","lastname":"%s","age":%s}""".formatted(firstName, lastname, age);
        }
    }

    @Test
    void mockPostAndRequestResource_verifyAndGetRequestBody_bodyMatches() {
        mockApi.when(() -> testMockedApi.create(anyBody()))
                .thenReturnDefault();
        TestDto testDto = new TestDto("albus", "dumbledore", 68);
        doCreate(testDto);

        TestDto actualRequestBody = mockApi.verify(() -> testMockedApi.create(anyBody()))
                .andGetRequestBody(TestDto.class);

        assertThat(actualRequestBody)
                .isEqualTo(testDto);
    }

    @Test
    void mockPostAndRequestResource_verifyAndGetRequestBodyAsString_bodyMatches() {
        mockApi.when(() -> testMockedApi.create(anyBody()))
                .thenReturnDefault();
        TestDto testDto = new TestDto("albus", "dumbledore", 68);
        doCreate(testDto);

        String body = mockApi.verify(() -> testMockedApi.create(anyBody()))
                .andGetRequestBodyAsString();

        assertThat(body).isEqualTo("{\"firstName\":\"albus\",\"lastname\":\"dumbledore\",\"age\":68}");
    }

    @Test
    void mockPostAndRequestResource_verifyAndGetRequest_requestMatches() {
        mockApi.when(() -> testMockedApi.create(anyBody()))
                .thenReturnDefault();
        TestDto testDto = new TestDto("albus", "dumbledore", 68);
        doCreate(testDto);

        Request<TestDto> actualRequest = mockApi.verify(() -> testMockedApi.create(anyBody()))
                .andGetRequest(TestDto.class);

        assertThat(actualRequest.path()).isEqualTo("/root-path/create");
        assertThat(actualRequest.body()).isEqualTo(testDto);
        assertExpectedQueryParams(actualRequest.queryParams());
        assertExpectedHeaders(actualRequest.headers());
    }

    @Test
    void mockPostAndRequestResource_verifyAndGetRequestWithBodyAsString_requestMatches() {
        mockApi.when(() -> testMockedApi.create(anyBody()))
                .thenReturnDefault();
        TestDto testDto = new TestDto("albus", "dumbledore", 68);
        doCreate(testDto);

        Request<BodyAsString> actualRequest = mockApi.verify(() -> testMockedApi.create(anyBody()))
                .andGetRequest();

        assertThat(actualRequest.body().value()).isEqualTo("{\"firstName\":\"albus\",\"lastname\":\"dumbledore\",\"age\":68}");
        assertThat(actualRequest.path()).isEqualTo("/root-path/create");
        assertExpectedQueryParams(actualRequest.queryParams());
        assertExpectedHeaders(actualRequest.headers());
    }

    @Test
    void mockPostAndRequestResourceTwice_verifyAndGetRequests_requestsMatch() {
        mockApi.when(() -> testMockedApi.create(anyBody()))
                .thenReturnDefault();

        TestDto testDtoOne = new TestDto("albus", "dumbledore", 68);
        TestDto testDtoTwo = new TestDto("minerva", "mcgonagall", 62);
        doCreate(testDtoOne);
        doCreate(testDtoTwo);

        List<Request<TestDto>> actualRequests = mockApi.verify(2, () -> testMockedApi.create(anyBody()))
                .andGetRequests(TestDto.class);

        assertThat(actualRequests).hasSize(2);
        assertThat(actualRequests).extracting(Request::path).allMatch("/root-path/create"::equals);
        assertThat(actualRequests).extracting(Request::queryParams).allSatisfy(AbstractRequestVerificationIntegrationTest::assertExpectedQueryParams);
        assertThat(actualRequests).extracting(Request::headers).allSatisfy(AbstractRequestVerificationIntegrationTest::assertExpectedHeaders);
        assertThat(actualRequests).extracting(Request::body)
                .containsExactly(testDtoOne, testDtoTwo);
    }

    @Test
    void mockPostAndRequestResourceTwice_verifyAndGetRequestWithBodyAsString_requestsMatch() {
        mockApi.when(() -> testMockedApi.create(anyBody()))
                .thenReturnDefault();

        TestDto testDtoOne = new TestDto("albus", "dumbledore", 68);
        TestDto testDtoTwo = new TestDto("minerva", "mcgonagall", 62);
        doCreate(testDtoOne);
        doCreate(testDtoTwo);

        List<Request<BodyAsString>> actualRequests = mockApi.verify(2, () -> testMockedApi.create(anyBody()))
                .andGetRequests();

        assertThat(actualRequests).hasSize(2);
        assertThat(actualRequests).extracting(Request::path).allMatch("/root-path/create"::equals);
        assertThat(actualRequests).extracting(Request::queryParams).allSatisfy(AbstractRequestVerificationIntegrationTest::assertExpectedQueryParams);
        assertThat(actualRequests).extracting(Request::headers).allSatisfy(AbstractRequestVerificationIntegrationTest::assertExpectedHeaders);
        assertThat(actualRequests).extracting(Request::body).extracting(BodyAsString::value)
                .containsExactly(
                        "{\"firstName\":\"albus\",\"lastname\":\"dumbledore\",\"age\":68}",
                        "{\"firstName\":\"minerva\",\"lastname\":\"mcgonagall\",\"age\":62}");
    }

    private void doCreate(TestDto testDto) {
        try {
            HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/create?q1=1&q2=2&q3"))
                                    .header("h1", "1")
                                    .header("h2", "2")
                                    .header("h3", "3")
                                    .POST(HttpRequest.BodyPublishers.ofString(testDto.toJson()))
                                    .build(),
                            BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertExpectedHeaders(HttpHeaders headers) {
        assertThat(headers.toMap())
                .containsEntry("h1", List.of("1"))
                .containsEntry("h2", List.of("2"))
                .containsEntry("h3", List.of("3"));
    }

    private static void assertExpectedQueryParams(QueryParams actual) {
        assertThat(actual).isEqualTo(
                QueryParams.builder()
                        .add("q1", "1")
                        .add("q2", "2")
                        .add("q3", "")
                        .build());
    }

}
