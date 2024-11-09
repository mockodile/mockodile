package io.github.mockodile.sb.it;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.mockodile.MockApi;
import io.github.mockodile.sb.EnableMockodile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = DYNAMIC_PORT)
@EnableMockodile
class HelloWorldMockedApiIntegrationTest {

    @Autowired
    private MockApi mockApi;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private HelloWorldApi helloWorldApi;

    @Test
    void springContextWithWiremockAndMockodileEnabled_initContext_contextInitialized() {
        assertThat(mockApi).isNotNull();
        assertThat(helloWorldApi).isNotNull();
    }

    @Test
    void autoConfiguredMockedApi_mockAndVerifyRequest_ok() {
        String baseUrl = String.format("http://localhost:%d", wireMockServer.port());
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(baseUrl)
                .build();

        mockApi.when(helloWorldApi.sayHello()).willReturn("hello-world");

        var responseEntity = restTemplate.getForEntity("/root-path/say-hello", String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo("\"hello-world\"");

        mockApi.verify(helloWorldApi.sayHello());
    }
}
