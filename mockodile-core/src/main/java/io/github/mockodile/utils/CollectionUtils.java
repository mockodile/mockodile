package io.github.mockodile.utils;

import java.util.Collection;
import java.util.Map;


public final class CollectionUtils {

    private CollectionUtils() {
        // do not construct
    }

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

}
