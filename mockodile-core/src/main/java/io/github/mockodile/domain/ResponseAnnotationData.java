package io.github.mockodile.domain;

public record ResponseAnnotationData(int defaultStatus, HttpHeaders headers) {

    public static ResponseAnnotationDataBuilder builder() {
        return new ResponseAnnotationDataBuilder();
    }
}
