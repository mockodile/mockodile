package io.github.mockodile;

import io.github.mockodile.argumentmatcher.ArgumentMatcher;
import io.github.mockodile.argumentmatcher.ArgumentMatcherType;
import io.github.mockodile.argumentmatcher.EqualityArgumentMatcher;
import io.github.mockodile.argumentmatcher.PathExpressionArgumentMatcher;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.ParamType;

public class PathBuilder {

    private final Invocation invocation;

    public PathBuilder(Invocation invocation) {
        this.invocation = invocation;
    }

    public BuiltPath build() {
        PathHolder pathHolder = new PathHolder(invocation.annotationData().requestAnnotationData().path());

        var pathParameters = invocation.getParameters().stream()
                .filter(p -> p.paramAnnotationData().paramType() == ParamType.PATH)
                .toList();

        pathParameters.forEach(p -> pathHolder.replace(p.paramAnnotationData().name(), toPathPattern(p.argumentMatcher())));

        // TODO rather than using a regex for the whole path only use a regex for the relevant parts of the path as specified
        // escape all static parts of the path with Pattern.quote()
        var usePathExpressionMatching = pathParameters.stream().anyMatch(p -> p.argumentMatcher().type() == ArgumentMatcherType.PATH_EXPRESSION);

        return new BuiltPath(pathHolder.path, usePathExpressionMatching);
    }

    private String toPathPattern(ArgumentMatcher argumentMatcher) {
        ArgumentMatcherType x = argumentMatcher.type();
        return switch (x) {
            case EQ -> ((EqualityArgumentMatcher<?>) argumentMatcher).value().toString();
            case PATH_EXPRESSION -> ((PathExpressionArgumentMatcher) argumentMatcher).expression();
            default ->
                    throw new IllegalArgumentException("Invalid argument matcher type for a path parameter, only EQ or PATH_EXPRESSION are supported but was " + argumentMatcher.type());
        };
    }

    public record BuiltPath(String path, boolean usePathExpressionMatching) {

    }

    private static class PathHolder {
        private String path;

        PathHolder(String path) {
            this.path = path;
        }

        void replace(String name, String value) {
            path = path.replace("{" + name + "}", value);
        }
    }

}