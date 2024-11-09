package io.github.mockodile.argumentmatcher;

public record AnyBodyMatcher() implements ArgumentMatcher {
    @Override
    public ArgumentMatcherType type() {
        return ArgumentMatcherType.ANY_BODY;
    }
}
