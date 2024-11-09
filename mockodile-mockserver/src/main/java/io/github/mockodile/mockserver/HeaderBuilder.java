package io.github.mockodile.mockserver;

import io.github.mockodile.argumentmatcher.ArgumentMatcher;
import io.github.mockodile.argumentmatcher.EqualityArgumentMatcher;
import io.github.mockodile.argumentmatcher.RegularExpressionArgumentMatcher;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.ParamType;
import org.mockserver.model.Header;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class HeaderBuilder {
    private final Invocation invocation;

    HeaderBuilder(Invocation invocation) {
        this.invocation = invocation;
    }

    List<Header> build() {
        return Stream.concat(
                        buildForStaticHeaders(invocation).stream(),
                        buildForParameters(invocation).stream())
                .toList();
    }

    private static List<Header> buildForStaticHeaders(Invocation invocation) {
        return invocation.annotationData().requestAnnotationData().headers()
                .toMap()
                .entrySet()
                .stream()
                .map(entry -> new Header(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static List<Header> buildForParameters(Invocation invocation) {
        return invocation.getParameters().stream()
                .filter(p -> p.paramAnnotationData().paramType() == ParamType.HEADER)
                .map(HeaderBuilder::toHeader)
                .toList();
    }

    private static Header toHeader(Invocation.InvokedParameter invokedParameter) {
        return new Header(invokedParameter.paramAnnotationData().name(), mapArgumentToValue(invokedParameter.argumentMatcher()));
    }

    private static String mapArgumentToValue(ArgumentMatcher argumentMatcher) {
        return switch (argumentMatcher.type()) {
            case EQ -> Pattern.quote(((EqualityArgumentMatcher<?>) argumentMatcher).value().toString());
            case REG_EX -> ((RegularExpressionArgumentMatcher) argumentMatcher).expression();
            default ->
                    throw new IllegalArgumentException("Invalid argument matcher type for a header parameter, only EQ or STRING_VALUE_PATTERN are supported but was " + argumentMatcher.type());
        };
    }

}
