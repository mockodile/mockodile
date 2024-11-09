package io.github.mockodile;

import java.net.URI;
import java.net.URISyntaxException;

final class TestUtil {

    private TestUtil() {
        // do not construct
    }

    static URI uri(String base, String path) {
        try {
            return new URI(base + path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
