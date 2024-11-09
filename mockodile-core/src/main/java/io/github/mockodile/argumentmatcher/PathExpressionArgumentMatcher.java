package io.github.mockodile.argumentmatcher;

public record PathExpressionArgumentMatcher(String expression) implements ArgumentMatcher {
    @Override
    public ArgumentMatcherType type() {
        return ArgumentMatcherType.PATH_EXPRESSION;
    }
}
