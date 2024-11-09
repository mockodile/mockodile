package io.github.mockodile.domain;

import java.util.List;

public class ResponseAnnotationDataBuilder {
    private int status = 200;
    private final HttpHeaders.HttpHeadersBuilder httpHeadersBuilder = HttpHeaders.builder();

    public ResponseAnnotationDataBuilder withStatus(int status) {
        this.status = status;
        return this;
    }

    public ResponseAnnotationDataBuilder withHeader(String headerName, String... headerValue) {
        httpHeadersBuilder.put(headerName, List.of(headerValue));
        return this;
    }

    public ResponseAnnotationDataBuilder withHeader(String headerName, List<String> headerValues) {
        httpHeadersBuilder.put(headerName, headerValues);
        return this;
    }

    public ResponseAnnotationDataBuilder withHeaders(List<String> headers) {
        httpHeadersBuilder.addAll(headers);
        return this;
    }

    public ResponseAnnotationData build() {
        return new ResponseAnnotationData(status, httpHeadersBuilder.build());
    }
}
