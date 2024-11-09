package io.github.mockodile.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import io.github.mockodile.ObjectMapper;
import io.github.mockodile.argumentmatcher.ArgumentMatcher;
import io.github.mockodile.argumentmatcher.ArgumentMatcherType;
import io.github.mockodile.argumentmatcher.EqualityArgumentMatcher;
import io.github.mockodile.argumentmatcher.RegularExpressionArgumentMatcher;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.ParamType;
import io.github.mockodile.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class BodyContentPatternBuilder {

    private final ObjectMapper objectMapper;
    private final Invocation invocation;

    BodyContentPatternBuilder(ObjectMapper objectMapper,
                              Invocation invocation) {
        this.objectMapper = objectMapper;
        this.invocation = invocation;
    }

    void applyTo(MappingBuilder mappingBuilder) {
        toStringValuePattern().ifPresent(mappingBuilder::withRequestBody);
    }

    void applyTo(RequestPatternBuilder requestPatternBuilder) {
        toStringValuePattern().ifPresent(requestPatternBuilder::withRequestBody);
    }

    Optional<StringValuePattern> toStringValuePattern() {

        // all found string value patterns on relevant annotated parameters will be combined with a conjunction.
        List<StringValuePattern> allStringValuePattern = new ArrayList<>();

        allStringValuePattern.addAll(requestBodyParams());
        allStringValuePattern.addAll(jsonPathParams());

        if (CollectionUtils.isEmpty(allStringValuePattern)) {
            return Optional.empty();
        }

        if (allStringValuePattern.size() == 1) {
            return Optional.of(allStringValuePattern.get(0));
        }

        return Optional.of(WireMock.and(allStringValuePattern.toArray(new StringValuePattern[0])));
    }

    private List<StringValuePattern> requestBodyParams() {
        return invocation.getParameters().stream()
                .filter(p1 -> p1.paramAnnotationData().paramType() == ParamType.REQUEST_BODY)
                // exclude ANY_BODY matchers as if the annotation did not exist
                .filter(p1 -> p1.argumentMatcher().type() != ArgumentMatcherType.ANY_BODY)
                .map(this::requestBodyToStringValuePattern)
                .toList();
    }

    private StringValuePattern requestBodyToStringValuePattern(Invocation.InvokedParameter invokedParameter) {
        return switch (invokedParameter.argumentMatcher().type()) {
            case EQ ->
                    WireMock.equalTo(encode(((EqualityArgumentMatcher<?>) invokedParameter.argumentMatcher()).value()));
            case REG_EX ->
                    WireMock.matching(((RegularExpressionArgumentMatcher) invokedParameter.argumentMatcher()).expression());
            default ->
                    throw new IllegalArgumentException("unsupported argument matcher for body parameter annotated as a request body");
        };
    }

    private List<StringValuePattern> jsonPathParams() {
        return invocation.getParameters().stream()
                .filter(p -> p.paramAnnotationData().paramType() == ParamType.JSON_PATH)
                .map(this::jsonPathToStringValuePattern)
                .toList();
    }

    private StringValuePattern jsonPathToStringValuePattern(Invocation.InvokedParameter p) {
        // the json path is stored in the name of the parameter, not so great
        var jsonPath = p.paramAnnotationData().name();
        var value = jsonPathValueToStringValuePattern(p.argumentMatcher());

        // no guarantee about the correctness of this json path
        return WireMock.matchingJsonPath(jsonPath, value);
    }

    private StringValuePattern jsonPathValueToStringValuePattern(ArgumentMatcher argumentMatcher) {
        return switch (argumentMatcher.type()) {
            case EQ -> WireMock.equalTo(((EqualityArgumentMatcher<?>) argumentMatcher).value().toString());
            case REG_EX -> WireMock.matching(((RegularExpressionArgumentMatcher) argumentMatcher).expression());
            default ->
                    throw new IllegalArgumentException("Invalid argument matcher type for a json path parameter, only EQ or STRING_VALUE_PATTERN are supported but was " + argumentMatcher.type());
        };
    }

    private String encode(Object value) {
        if (value instanceof CharSequence) {
            return value.toString();
        } else {
            // currently only supported conversion for bodies is to json using the supplied object mapper
            return objectMapper.writeValueAsString(value);
        }
    }
}
