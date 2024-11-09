package io.github.mockodile.sb;

import io.github.mockodile.MockApi;
import io.github.mockodile.mockserver.MockApiFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class MockodileConfiguration {

    @Bean
    @Lazy
    MockApi mockApi(MockServerClientHolder mockServerProvider) {
        return MockApiFactory.from(mockServerProvider);
    }
}
