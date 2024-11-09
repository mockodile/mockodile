package io.github.mockodile.argumentmatcher;

public record EqualityArgumentMatcher<T>(T value) implements ArgumentMatcher {
    @Override
    public ArgumentMatcherType type() {
        return ArgumentMatcherType.EQ;
    }
}
