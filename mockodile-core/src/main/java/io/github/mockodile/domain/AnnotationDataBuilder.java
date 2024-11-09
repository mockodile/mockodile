package io.github.mockodile.domain;

import java.util.function.Consumer;

public record AnnotationDataBuilder(RequestAnnotationDataBuilder requestAnnotationDataBuilder, ResponseAnnotationDataBuilder responseAnnotationDataBuilder) {

    public AnnotationData build() {
        return new AnnotationData(requestAnnotationDataBuilder.build(), responseAnnotationDataBuilder.build());
    }

    public AnnotationDataBuilder request(Consumer<RequestAnnotationDataBuilder> requestCustomizer) {
        requestCustomizer.accept(requestAnnotationDataBuilder);
        return this;
    }

    public AnnotationDataBuilder response(Consumer<ResponseAnnotationDataBuilder> responseCustomizer) {
        responseCustomizer.accept(responseAnnotationDataBuilder);
        return this;
    }

}
