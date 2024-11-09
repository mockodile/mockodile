package io.github.mockodile.domain;

public record Response<T>(int status, HttpHeaders headers, Body<T> body) {

    public static <T> ResponseBuilder<T> builder() {
        return new ResponseBuilder<>();
    }

    public static class ResponseBuilder<T> {
        private final HttpHeaders.HttpHeadersBuilder headersBuilder = HttpHeaders.builder();
        private int status = 200;
        private Body<T> body;

        public ResponseBuilder<T> withStatus(int status) {
            this.status = status;
            return this;
        }

        public ResponseBuilder<T> withBody(T body) {
            this.body = new Body<>(body);
            return this;
        }

        public ResponseBuilder<T> withBody(String s) {
            this.body = new Body<>(s);
            return this;
        }

        public ResponseBuilder<T> withBody(byte[] raw) {
            this.body = new Body<>(raw);
            return this;
        }

        public ResponseBuilder<T> withHeaders(HttpHeaders headers) {
            headersBuilder.addAll(headers);
            return this;
        }

        public ResponseBuilder<T> withHeader(String name, String value) {
            headersBuilder.add(name, value);
            return this;
        }

        public Response<T> build() {
            return new Response<>(status, headersBuilder.build(), body);
        }
    }
}
