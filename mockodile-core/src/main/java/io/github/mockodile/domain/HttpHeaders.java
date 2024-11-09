package io.github.mockodile.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpHeaders {

    private static final HttpHeaders EMPTY = HttpHeaders.builder().build();

    private final MultiMap<String, String> map;

    HttpHeaders(MultiMap<String, String> map) {
        this.map = map;
    }

    public static HttpHeadersBuilder builder() {
        return new HttpHeadersBuilder();
    }

    public Map<String, List<String>> toMap() {
        return map.toMap();
    }

    public static HttpHeaders empty() {
        return EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpHeaders that = (HttpHeaders) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "HttpHeaders{" +
                "map=" + map +
                '}';
    }

    public static class HttpHeadersBuilder {

        private final MultiMap.MultiMapBuilder<String, String> multiMapBuilder = MultiMap.builder();

        private HttpHeadersBuilder() {
        }

        public HttpHeadersBuilder put(String header, List<String> value) {
            multiMapBuilder.put(header, Collections.unmodifiableList(value));
            return this;
        }

        public HttpHeadersBuilder add(String header, String value) {
            multiMapBuilder.add(header, value);
            return this;
        }

        public HttpHeadersBuilder add(String stringWithEqualsSplit) {
            String[] split = stringWithEqualsSplit.split("=", 2);
            if (split.length == 1) {
                add(split[0], "");
            } else {
                add(split[0], split[1]);
            }
            return this;
        }

        public HttpHeadersBuilder addAll(List<String> stringsWithEqualsSplit) {
            stringsWithEqualsSplit.forEach(this::add);
            return this;
        }

        public HttpHeadersBuilder addAll(HttpHeaders headers) {
            headers.toMap().forEach(this::put);
            return this;
        }

        public HttpHeaders build() {
            return new HttpHeaders(multiMapBuilder.build());
        }
    }
}
