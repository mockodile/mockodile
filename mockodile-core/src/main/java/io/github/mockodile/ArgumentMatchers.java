package io.github.mockodile;

import io.github.mockodile.argumentmatcher.AnyBodyMatcher;
import io.github.mockodile.argumentmatcher.EqualityArgumentMatcher;
import io.github.mockodile.argumentmatcher.PathExpressionArgumentMatcher;
import io.github.mockodile.argumentmatcher.RegularExpressionArgumentMatcher;

public final class ArgumentMatchers {

    private ArgumentMatchers() {
        // do not construct
    }

    public static <T> T eq(T value) {
        ThreadSafeMockingState.mockingState().registerArgumentMatcher(new EqualityArgumentMatcher<>(value));
        return null;
    }

    public static <T> T anyPath() {
        return pathMatches(".*");
    }

    public static <T> T pathMatches(String expression) {
        ThreadSafeMockingState.mockingState().registerArgumentMatcher(new PathExpressionArgumentMatcher(expression));
        return null;
    }

    public static <T> T matchesAnyString() {
        return matches(".*");
    }

    public static <T> T matches(String expression) {
        ThreadSafeMockingState.mockingState().registerArgumentMatcher(new RegularExpressionArgumentMatcher(expression));
        return null;
    }

    public static <T> T anyBody() {
        ThreadSafeMockingState.mockingState().registerArgumentMatcher(new AnyBodyMatcher());
        return null;
    }

}
