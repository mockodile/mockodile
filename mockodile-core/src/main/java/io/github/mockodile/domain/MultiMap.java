package io.github.mockodile.domain;

import java.util.*;

class MultiMap<K, V> {

    private static final MultiMap<?, ?> EMPTY = MultiMap.builder().build();

    private final Map<K, List<V>> map;

    MultiMap(Map<K, List<V>> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    static <K, V> MultiMapBuilder<K, V> builder() {
        return new MultiMapBuilder<>();
    }

    Map<K, List<V>> toMap() {
        return map;
    }

    @SuppressWarnings("unchecked")
    static <K, V> MultiMap<K, V> empty() {
        return (MultiMap<K, V>) EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiMap<?, ?> that = (MultiMap<?, ?>) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "MultiMap{" +
                "map=" + map +
                '}';
    }

    static class MultiMapBuilder<K, V> {

        private final Map<K, List<V>> map = new HashMap<>();

        private MultiMapBuilder() {
        }

        MultiMapBuilder<K, V> put(K key, List<V> value) {
            map.put(key, Collections.unmodifiableList(value));
            return this;
        }

        MultiMapBuilder<K, V> add(K key, V value) {
            List<V> newValues = new ArrayList<>();
            if (map.containsKey(key)) {
                newValues.addAll(map.get(key));
            }
            newValues.add(value);
            return put(key, newValues);
        }

        MultiMapBuilder<K, V> addAll(MultiMap<K, V> map) {
            map.toMap().forEach(this::put);
            return this;
        }

        MultiMap<K, V> build() {
            return new MultiMap<>(map);
        }
    }

}
