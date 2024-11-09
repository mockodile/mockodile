package io.github.mockodile.mockserver;

import io.github.mockodile.ObjectMapper;
import io.github.mockodile.PathBuilder;
import io.github.mockodile.domain.Invocation;
import org.mockserver.model.HttpRequest;

import java.util.regex.Pattern;

import static org.mockserver.model.HttpRequest.request;

class RequestBuilder {

    private final ObjectMapper objectMapper;
    private final Invocation invocation;

    RequestBuilder(ObjectMapper objectMapper,
                          Invocation invocation) {
        this.objectMapper = objectMapper;
        this.invocation = invocation;
    }

    HttpRequest build() {
        var builtPath = new PathBuilder(invocation).build();

        var request = request()
                .withMethod(invocation.annotationData().requestAnnotationData().httpMethod().name())
                .withPath(builtPath.usePathExpressionMatching() ? builtPath.path() : Pattern.quote(builtPath.path()))
                .withHeaders(new HeaderBuilder(invocation).build())
                .withQueryStringParameters(new QueryParameterBuilder(invocation).build());

        var bodyMatcher = new BodyBuilder(objectMapper, invocation).build();
        if (bodyMatcher.isPresent()) {
            request = request.withBody(bodyMatcher.get());
        }

        return request;
    }

}
