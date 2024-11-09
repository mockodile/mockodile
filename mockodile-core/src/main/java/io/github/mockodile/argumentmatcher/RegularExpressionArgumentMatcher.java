package io.github.mockodile.argumentmatcher;

public record RegularExpressionArgumentMatcher(String expression) implements ArgumentMatcher {
    @Override
    public ArgumentMatcherType type() {
        return ArgumentMatcherType.REG_EX;
    }
}
