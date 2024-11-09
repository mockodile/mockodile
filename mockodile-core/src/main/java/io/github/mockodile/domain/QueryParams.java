package io.github.mockodile.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import static java.util.stream.Collectors.*;

public class QueryParams {

    private static final QueryParams EMPTY = QueryParams.builder().build();

    private final MultiMap<String, String> map;

    QueryParams(MultiMap<String, String> map) {
        this.map = map;
    }

    public static QueryParamsBuilder builder() {
        return new QueryParamsBuilder();
    }

    public static QueryParams from(String s) {
        var builder = builder();
        splitQuery(s)
                .forEach(builder::put);
        return builder.build();
    }

    public Map<String, List<String>> toMap() {
        return map.toMap();
    }

    public static QueryParams empty() {
        return EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryParams that = (QueryParams) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "QueryParams{" +
                "map=" + map +
                '}';
    }

    /**
     * Decode parameters in query part of a URI into a map from parameter name to its parameter values.
     * For parameters that occur multiple times each value is collected.
     * Proper decoding of the parameters is performed.
     * <br/>
     * Example
     *   <pre>a=1&b=2&c=&a=4</pre>
     * is converted into
     *   <pre>{a=["1", "4"], b=["2"], c=[""]}</pre>
     * @param query the query part of an URI
     * @return map of parameters names into a list of their values.
     */
    static Map<String, List<String>> splitQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }

        return Arrays.stream(query.split("&"))
                .map(QueryParams::splitQueryParameter)
                .collect(groupingBy(Pair::get0, mapping(Pair::get1, toList())));
    }

    static Pair<String, String> splitQueryParameter(String parameter) {
        final String enc = "UTF-8";
        List<String> keyValue = Arrays.stream(parameter.split("=", 2))
                .map(e -> {
                    try {
                        return URLDecoder.decode(e, enc);
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeUnsupportedEncodingException(ex);
                    }
                }).toList();

        if (keyValue.size() == 2) {
            return new Pair<>(keyValue.get(0), keyValue.get(1));
        } else {
            return new Pair<>(keyValue.get(0), "");
        }
    }

    /** Runtime exception (instead of checked exception) to denote unsupported enconding */
    public static class RuntimeUnsupportedEncodingException extends RuntimeException {
        public RuntimeUnsupportedEncodingException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * A simple pair of two elements
     * @param <U> first element
     * @param <V> second element
     */
    private static class Pair<U, V> {
        U a;
        V b;

        public Pair(U u, V v) {
            this.a = u;
            this.b = v;
        }

        public U get0() {
            return a;
        }

        public V get1() {
            return b;
        }
    }

    public static class QueryParamsBuilder {

        private final MultiMap.MultiMapBuilder<String, String> multiMapBuilder = MultiMap.builder();

        private QueryParamsBuilder() {
        }

        public QueryParamsBuilder put(String param, List<String> value) {
            multiMapBuilder.put(param, Collections.unmodifiableList(value));
            return this;
        }

        public QueryParamsBuilder add(String param, String value) {
            multiMapBuilder.add(param, value);
            return this;
        }

        public QueryParamsBuilder addAll(QueryParams params) {
            params.toMap().forEach(this::put);
            return this;
        }

        public QueryParams build() {
            return new QueryParams(multiMapBuilder.build());
        }
    }
}
