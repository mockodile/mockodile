package io.github.mockodile;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.RequestMapping;
import io.github.mockodile.domain.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractCustomResponseIntegrationTest extends AbstractIntegrationTest{

    private TestMockedApi testMockedApi;
    private final ObjectMapper objectMapper = ObjectMapper.defaultObjectMapper();

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    record PersonTestDto(String firstName, String lastName, int age) {
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {
        @Get(path = "/person")
        PersonTestDto getPerson();
    }

    @Test
    void mockGetWithResponseWithCustomDto_requestResource_specifiedResponseReturned() {
        PersonTestDto personTestData = new PersonTestDto("Ron", "Weasley", 11);
        mockApi.when(testMockedApi.getPerson()).willReturn(personTestData);

        var personResponse = getPerson();

        assertThat(personResponse.status).isEqualTo(200);
        assertThat(personResponse.person).isEqualTo(personTestData);
    }

    @Test
    void mockGetWithResponseWithCustomStatusAndDto_requestResource_specifiedResponseReturned() {
        PersonTestDto personTestData = new PersonTestDto("Fred", "Weasley", 14);
        mockApi.when(testMockedApi.getPerson()).willReturn(201, personTestData, HttpHeaders.builder().build());

        var personResponse = getPerson();

        assertThat(personResponse.status).isEqualTo(201);
        assertThat(personResponse.person).isEqualTo(personTestData);
    }

    @Test
    void mockGetWithResponseWithAdditionalHeader_requestResource_specifiedResponseReturned() {
        PersonTestDto personTestData = new PersonTestDto("George", "Weasley", 14);
        mockApi.when(testMockedApi.getPerson()).willReturn(201, personTestData, HttpHeaders.builder().add("x-house", "the burrow").build());

        var personResponse = getPerson();

        assertThat(personResponse.status).isEqualTo(201);
        assertThat(personResponse.headers).containsKey("x-house");
        assertThat(personResponse.headers.get("x-house")).containsExactly("the burrow");
        assertThat(personResponse.person).isEqualTo(personTestData);
    }

    private PersonResponse getPerson() {
        try {
            var response = HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder()
                                    .uri(TestUtil.uri(mockUrl, "/root-path/person"))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());

            var person = objectMapper.readValue(response.body(), PersonTestDto.class);
            return new PersonResponse(response.statusCode(), response.headers().map(), person);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    record PersonResponse(int status, Map<String, List<String>> headers, PersonTestDto person) {}
}
