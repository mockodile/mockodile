package io.github.mockodile.wiremock;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.mockodile.MockApiProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

public class MockodileWiremockExtension extends WireMockExtension {

    public MockodileWiremockExtension() {
        super(WireMockExtension.newInstance()
                .options(WireMockConfiguration.wireMockConfig()
                        .dynamicPort()
                        .dynamicHttpsPort()
                        .notifier(new ConsoleNotifier(true))));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return super.supportsParameter(parameterContext, extensionContext) || isMockApiProviderParameter(parameterContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (isMockApiProviderParameter(parameterContext)) {
            var wireMockRuntimeInfo = getRuntimeInfo();
            var mockApi = MockApiFactory.from(wireMockRuntimeInfo.getWireMock());
            int port = wireMockRuntimeInfo.getHttpPort();
            return new MockApiProvider(mockApi, port);
        }
        return super.resolveParameter(parameterContext, extensionContext);
    }

    private static boolean isMockApiProviderParameter(ParameterContext parameterContext) {
        return parameterContext.getParameter().getType().equals(MockApiProvider.class);
    }
}
