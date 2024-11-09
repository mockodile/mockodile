package io.github.mockodile.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import io.github.mockodile.PathBuilder;
import io.github.mockodile.domain.Invocation;

class UrlPathPatternBuilder {

    private final Invocation invocation;

    UrlPathPatternBuilder(Invocation invocation) {
        this.invocation = invocation;
    }

    UrlPathPattern build() {
        var builtPath = new PathBuilder(invocation).build();
        return builtPath.usePathExpressionMatching() ? WireMock.urlPathMatching(builtPath.path()) : WireMock.urlPathEqualTo(builtPath.path());
    }
}
