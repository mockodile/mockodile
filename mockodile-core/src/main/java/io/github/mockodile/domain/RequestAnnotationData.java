package io.github.mockodile.domain;

import java.util.List;

/**
 * abstraction for the data found on the annotations on the type and the method.
 */
public record RequestAnnotationData(HttpMethod httpMethod,
                                    String path,
                                    HttpHeaders headers,
                                    List<ParamAnnotationData> paramAnnotationData) {

    public static RequestAnnotationDataBuilder builder() {
        return new RequestAnnotationDataBuilder();
    }
}
