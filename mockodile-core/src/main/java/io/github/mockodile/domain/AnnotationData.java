package io.github.mockodile.domain;

public record AnnotationData(RequestAnnotationData requestAnnotationData, ResponseAnnotationData responseAnnotationData) {

    public static AnnotationDataBuilder builder() {
        return new AnnotationDataBuilder(RequestAnnotationData.builder(), ResponseAnnotationData.builder());
    }
}
