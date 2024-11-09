package io.github.mockodile.utils;

public final class StringUtils {
    private StringUtils() {
        // do not construct
    }

    public static boolean hasText(String str) {
        return (str != null && !str.isBlank());
    }
}
