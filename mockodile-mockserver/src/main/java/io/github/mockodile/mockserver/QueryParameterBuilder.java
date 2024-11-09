package io.github.mockodile.mockserver;

import io.github.mockodile.argumentmatcher.ArgumentMatcher;
import io.github.mockodile.argumentmatcher.EqualityArgumentMatcher;
import io.github.mockodile.argumentmatcher.RegularExpressionArgumentMatcher;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.ParamType;
import org.mockserver.model.Parameter;

import java.util.List;
import java.util.regex.Pattern;

class QueryParameterBuilder {
    private final Invocation invocation;

    QueryParameterBuilder(Invocation invocation) {
        this.invocation = invocation;
    }

    List<Parameter> build() {
        return invocation.getParameters().stream().filter(p -> p.paramAnnotationData().paramType() == ParamType.QUERY)
                .map(this::mapParameter)
                .toList();
    }

    private Parameter mapParameter(Invocation.InvokedParameter invokedParameter) {
        return new Parameter(invokedParameter.paramAnnotationData().name(), mapArgumentToValue(invokedParameter.argumentMatcher()));
    }

    private String mapArgumentToValue(ArgumentMatcher argumentMatcher) {
        return switch (argumentMatcher.type()) {
            case EQ -> Pattern.quote(((EqualityArgumentMatcher<?>) argumentMatcher).value().toString());
            case REG_EX -> ((RegularExpressionArgumentMatcher) argumentMatcher).expression();
            default ->
                    throw new IllegalArgumentException("Invalid argument matcher type for a query parameter, only EQ or STRING_VALUE_PATTERN are supported but was " + argumentMatcher.type());
        };
    }
}
