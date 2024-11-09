package io.github.mockodile.mockserver;

import io.github.mockodile.MockApiProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.mockserver.junit.jupiter.MockServerExtension;

public class MockodileMockServerExtension extends MockServerExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return super.supportsParameter(parameterContext, extensionContext) || isMockApiProviderParameter(parameterContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (isMockApiProviderParameter(parameterContext)) {
            var mockApi = MockApiFactory.from(clientAndServer);
            int port = clientAndServer.getPort();
            return new MockApiProvider(mockApi, port);
        }
        return super.resolveParameter(parameterContext, extensionContext);
    }

    private static boolean isMockApiProviderParameter(ParameterContext parameterContext) {
        return parameterContext.getParameter().getType().equals(MockApiProvider.class);
    }

}
