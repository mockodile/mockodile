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

import java.util.List;

class QueryParameterBuilder {

    private final Invocation invocation;

    QueryParameterBuilder(Invocation invocation) {
        this.invocation = invocation;
    }

    void applyTo(MappingBuilder mappingBuilder) {
        toList().forEach(p -> mappingBuilder.withQueryParam(p.name(), p.stringValuePattern()));
    }

    void applyTo(RequestPatternBuilder requestPatternBuilder) {
        toList().forEach(p -> requestPatternBuilder.withQueryParam(p.name(), p.stringValuePattern()));
    }

    private List<QueryParameterStringValuePatternEntry> toList() {
        return invocation.getParameters().stream().filter(p -> p.paramAnnotationData().paramType() == ParamType.QUERY)
                .map(this::mapParameter)
                .toList();
    }

    private QueryParameterStringValuePatternEntry mapParameter(Invocation.InvokedParameter invokedParameter) {
        return new QueryParameterStringValuePatternEntry(invokedParameter.paramAnnotationData().name(), toStringValuePattern(invokedParameter.argumentMatcher()));
    }

    private StringValuePattern toStringValuePattern(ArgumentMatcher argumentMatcher) {
        return switch (argumentMatcher.type()) {
            case EQ -> WireMock.equalTo(((EqualityArgumentMatcher<?>) argumentMatcher).value().toString());
            case REG_EX -> WireMock.matching(((RegularExpressionArgumentMatcher) argumentMatcher).expression());
            default ->
                    throw new IllegalArgumentException("Invalid argument matcher type for a query parameter, only EQ or STRING_VALUE_PATTERN are supported but was " + argumentMatcher.type());
        };
    }


    private record QueryParameterStringValuePatternEntry(String name, StringValuePattern stringValuePattern) {
    }

}
