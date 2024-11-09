package io.github.mockodile.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import io.github.mockodile.argumentmatcher.ArgumentMatcher;
import io.github.mockodile.argumentmatcher.EqualityArgumentMatcher;
import io.github.mockodile.argumentmatcher.RegularExpressionArgumentMatcher;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.ParamType;

import java.util.ArrayList;
import java.util.List;

class HeaderBuilder {

    private final Invocation invocation;

    HeaderBuilder(Invocation invocation) {
        this.invocation = invocation;
    }

    void applyTo(MappingBuilder mappingBuilder) {
        toHeaderMatchers().forEach(headerMatcher -> mappingBuilder.withHeader(headerMatcher.name, headerMatcher.stringValuePattern));
    }

    void applyTo(RequestPatternBuilder requestPatternBuilder) {
        toHeaderMatchers().forEach(headerMatcher -> requestPatternBuilder.withHeader(headerMatcher.name, headerMatcher.stringValuePattern));
    }

    private List<HeaderStringValuePatternEntry> toHeaderMatchers() {
        List<HeaderStringValuePatternEntry> headerMatchers = new ArrayList<>();
        headerMatchers.addAll(buildForStaticHeaders(invocation));
        headerMatchers.addAll(buildForParameters(invocation));
        return headerMatchers;
    }

    private static List<HeaderStringValuePatternEntry> buildForStaticHeaders(Invocation invocation) {
        List<HeaderStringValuePatternEntry> headerMatchers = new ArrayList<>();

        invocation.annotationData().requestAnnotationData().headers()
                .toMap()
                .forEach((key, value1) -> value1.forEach(value -> headerMatchers.add(new HeaderStringValuePatternEntry(key, WireMock.equalTo(value)))));

        return headerMatchers;
    }

    private List<HeaderStringValuePatternEntry> buildForParameters(Invocation invocation) {
        return invocation.getParameters().stream().filter(p -> p.paramAnnotationData().paramType() == ParamType.HEADER)
                .map(this::mapParameter)
                .toList();
    }

    private HeaderStringValuePatternEntry mapParameter(Invocation.InvokedParameter invokedParameter) {
        return new HeaderStringValuePatternEntry(invokedParameter.paramAnnotationData().name(), toStringValuePattern(invokedParameter.argumentMatcher()));
    }

    private static StringValuePattern toStringValuePattern(ArgumentMatcher argumentMatcher) {
        return switch (argumentMatcher.type()) {
            case EQ -> WireMock.equalTo(((EqualityArgumentMatcher<?>) argumentMatcher).value().toString());
            case REG_EX -> WireMock.matching(((RegularExpressionArgumentMatcher) argumentMatcher).expression());
            default ->
                    throw new IllegalArgumentException("Invalid argument matcher type for a header parameter, only EQ or STRING_VALUE_PATTERN are supported but was " + argumentMatcher.type());
        };
    }

    private record HeaderStringValuePatternEntry(String name, StringValuePattern stringValuePattern) {
    }
}
