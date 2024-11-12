package io.github.mockodile.mockserver;

import io.github.mockodile.ObjectMapper;
import io.github.mockodile.argumentmatcher.ArgumentMatcherType;
import io.github.mockodile.argumentmatcher.EqualityArgumentMatcher;
import io.github.mockodile.argumentmatcher.RegularExpressionArgumentMatcher;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.ParamType;
import org.mockserver.model.Body;
import org.mockserver.model.RegexBody;
import org.mockserver.model.StringBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockserver.model.JsonPathBody.jsonPath;

class BodyBuilder {

    private final ObjectMapper objectMapper;
    private final Invocation invocation;

    BodyBuilder(ObjectMapper objectMapper, Invocation invocation) {
        this.objectMapper = objectMapper;
        this.invocation = invocation;
    }

    public Optional<Body<String>> build() {
        var jsonPathParameters = invocation.getParameters().stream()
                .filter(p -> p.paramAnnotationData().paramType() == ParamType.JSON_PATH)
                .toList();

        var bodyParameters = invocation.getParameters().stream()
                .filter(p1 -> p1.paramAnnotationData().paramType() == ParamType.REQUEST_BODY)
                // exclude ANY_BODY matchers as if the annotation did not exist
                .filter(p1 -> p1.argumentMatcher().type() != ArgumentMatcherType.ANY_BODY)
                .toList();

        if (!jsonPathParameters.isEmpty() && !bodyParameters.isEmpty()) {
            throw new IllegalArgumentException("MockServer cannot combine a body matcher with a json path matcher, it anyway doesn't make any sense");
        }

        if (!jsonPathParameters.isEmpty()) {
            return Optional.of(mapToJsonPath(jsonPathParameters));
        }

        if (!bodyParameters.isEmpty()) {
            return Optional.of(mapToBodyMatcher(bodyParameters));
        }

        return Optional.empty();
    }

    private Body<String> mapToBodyMatcher(List<Invocation.InvokedParameter> bodyParameters) {
        if (bodyParameters.size() > 1) {
            throw new IllegalArgumentException("MockServer doesn't support multiple body matchers, it anyway doesn't make any sense");
        }

        var bodyParameter = bodyParameters.get(0);

        // TODO add support for mockserver strict / non-strict json matching
        return switch (bodyParameter.argumentMatcher().type()) {
            case EQ ->
                    StringBody.exact(encode(((EqualityArgumentMatcher<?>) bodyParameter.argumentMatcher()).value()));
            case REG_EX ->
                    RegexBody.regex(((RegularExpressionArgumentMatcher) bodyParameter.argumentMatcher()).expression());
            default ->
                    throw new IllegalArgumentException("unsupported argument matcher for body parameter annotated as a request body");
        };
    }

    private Body<String> mapToJsonPath(List<Invocation.InvokedParameter> jsonPathParameters) {
        // joins all conditions into one Body with a json path such as [?($.firstName=='John' && $.lastName=='doe')]
        // where regex are used will be [?($.firstName=~/[Jj]ohn && $.lastName=~'[Dd]oe')]
        var jsonPathExpressionsConjoined = jsonPathParameters.stream()
                    .map(BodyBuilder::toExpression)
                    .collect(Collectors.joining("&&"));
        var fullJsonPathExpression = "[?(" + jsonPathExpressionsConjoined + ")]";
        return jsonPath(fullJsonPathExpression);
    }

    private static String toExpression(Invocation.InvokedParameter bodyParameter) {
        // the json path is stored in the name of the parameter, not so great
        var jsonPath = bodyParameter.paramAnnotationData().name();

        return switch (bodyParameter.argumentMatcher().type()) {
            case EQ -> toEqExpression((EqualityArgumentMatcher<?>) bodyParameter.argumentMatcher(), jsonPath);
            case REG_EX ->
                    String.format("%s=~/%s/", jsonPath, ((RegularExpressionArgumentMatcher) bodyParameter.argumentMatcher()).expression());
            default ->
                    throw new IllegalArgumentException("Invalid argument matcher type for a json path parameter, only EQ or STRING_VALUE_PATTERN are supported but was " + bodyParameter.argumentMatcher().type());
        };
    }

    private static String toEqExpression(EqualityArgumentMatcher<?> argumentMatcher, String jsonPath) {
        if (argumentMatcher.value() instanceof Boolean b) {
            return String.format("%s==%s", jsonPath, b);
        } else if (argumentMatcher.value() instanceof Number n) {
            return String.format("%s==%s", jsonPath, n);
        } else {
            return String.format("%s=='%s'", jsonPath, argumentMatcher.value().toString());
        }
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
