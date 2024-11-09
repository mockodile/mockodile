package io.github.mockodile.domain;

public record Request<T>(String path, HttpHeaders headers, QueryParams queryParams, T body) {

    public static <T> RequestBuilder<T> builder() {
        return new RequestBuilder<>();
    }

    public static class RequestBuilder<T> {
        private String path;
        private HttpHeaders headers = HttpHeaders.empty();
        private QueryParams queryParams = QueryParams.empty();
        private T body;

        private RequestBuilder() {
        }

        public RequestBuilder<T> withPath(String path) {
            this.path = path;
            return this;
        }

        public RequestBuilder<T> withHeaders(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public RequestBuilder<T> withQueryParams(QueryParams queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public RequestBuilder<T> withBody(T t) {
            this.body = t;
            return this;
        }

        public Request<T> build() {
            return new Request<>(path, headers, queryParams, body);
        }

    }

}
