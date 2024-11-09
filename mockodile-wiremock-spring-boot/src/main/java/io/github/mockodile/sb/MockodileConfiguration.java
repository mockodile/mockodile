package io.github.mockodile.sb;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.mockodile.MockApi;
import io.github.mockodile.wiremock.MockApiFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class MockodileConfiguration {

    @Bean
    @Lazy
    MockApi mockApi(WireMockServer wireMockServer) {
        return MockApiFactory.from(wireMockServer);
    }
}
