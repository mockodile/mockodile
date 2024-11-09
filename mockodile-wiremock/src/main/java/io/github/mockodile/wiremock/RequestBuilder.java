package io.github.mockodile.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import io.github.mockodile.ObjectMapper;
import io.github.mockodile.domain.Invocation;

public class RequestBuilder {

    private final ObjectMapper objectMapper;
    private final Invocation invocation;

    public RequestBuilder(ObjectMapper objectMapper,
                   Invocation invocation) {
        this.objectMapper = objectMapper;
        this.invocation = invocation;
    }

    public MappingBuilder toMappingBuilder() {
        UrlPathPattern urlPathPattern = new UrlPathPatternBuilder(invocation).build();

        var mappingBuilder = switch (invocation.annotationData().requestAnnotationData().httpMethod()) {
            case GET -> WireMock.get(urlPathPattern);
            case HEAD -> WireMock.head(urlPathPattern);
            case POST -> WireMock.post(urlPathPattern);
            case PUT -> WireMock.put(urlPathPattern);
            case PATCH -> WireMock.patch(urlPathPattern);
            case DELETE -> WireMock.delete(urlPathPattern);
            case OPTIONS -> WireMock.options(urlPathPattern);
            case TRACE -> WireMock.trace(urlPathPattern);
        };

        new HeaderBuilder(invocation).applyTo(mappingBuilder);
        new QueryParameterBuilder(invocation).applyTo(mappingBuilder);
        new BodyContentPatternBuilder(objectMapper, invocation).applyTo(mappingBuilder);

        return mappingBuilder;
    }

    public RequestPatternBuilder toRequestPatternBuilder() {
        UrlPathPattern urlPathPattern = new UrlPathPatternBuilder(invocation).build();


        var requestPatternBuilder = switch (invocation.annotationData().requestAnnotationData().httpMethod()) {
            case GET -> WireMock.getRequestedFor(urlPathPattern);
            case HEAD -> WireMock.headRequestedFor(urlPathPattern);
            case POST -> WireMock.postRequestedFor(urlPathPattern);
            case PUT -> WireMock.putRequestedFor(urlPathPattern);
            case PATCH -> WireMock.patchRequestedFor(urlPathPattern);
            case DELETE -> WireMock.deleteRequestedFor(urlPathPattern);
            case OPTIONS -> WireMock.optionsRequestedFor(urlPathPattern);
            case TRACE -> WireMock.traceRequestedFor(urlPathPattern);
        };

        new HeaderBuilder(invocation).applyTo(requestPatternBuilder);
        new QueryParameterBuilder(invocation).applyTo(requestPatternBuilder);
        new BodyContentPatternBuilder(objectMapper, invocation).applyTo(requestPatternBuilder);

        return requestPatternBuilder;
    }
}
