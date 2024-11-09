package io.github.mockodile.annotations;

import io.github.mockodile.domain.ParamType;
import io.github.mockodile.domain.RequestAnnotationDataBuilder;
import io.github.mockodile.utils.StringUtils;

import java.lang.annotation.Annotation;

public class ParamAnnotationUtil {

    private ParamAnnotationUtil() {
        // do not construct
    }

    public static boolean isParamAnnotation(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(ParamAnnotation.class);
    }

    public static void applyAnnotationTo(Annotation paramAnnotation, String parameterName, RequestAnnotationDataBuilder builder) {
        switch (getParamType(paramAnnotation)) {
            case PATH -> applyPathParam((PathParam) paramAnnotation, parameterName, builder);
            case HEADER -> applyHeaderParam((HeaderParam) paramAnnotation, parameterName, builder);
            case QUERY -> applyQueryParam((QueryParam) paramAnnotation, parameterName, builder);
            case JSON_PATH -> applyJsonPathParam((JsonPath) paramAnnotation, builder);
            case REQUEST_BODY -> applyRequestBodyParam(parameterName, builder);
        }
    }

    private static void applyPathParam(PathParam pathParam, String parameterName, RequestAnnotationDataBuilder builder) {
        builder.withParam(ParamType.PATH, StringUtils.hasText(pathParam.value()) ? pathParam.value() : parameterName);
    }

    private static void applyHeaderParam(HeaderParam headerParam, String parameterName, RequestAnnotationDataBuilder builder) {
        builder.withParam(ParamType.HEADER, StringUtils.hasText(headerParam.value()) ? headerParam.value() : parameterName);
    }

    private static void applyQueryParam(QueryParam queryParam, String parameterName, RequestAnnotationDataBuilder builder) {
        builder.withParam(ParamType.QUERY, StringUtils.hasText(queryParam.value()) ? queryParam.value() : parameterName);
    }

    private static void applyJsonPathParam(JsonPath jsonPath, RequestAnnotationDataBuilder builder) {
        builder.withParam(ParamType.JSON_PATH, jsonPath.expression());
    }

    private static void applyRequestBodyParam(String parameterName, RequestAnnotationDataBuilder builder) {
        builder.withParam(ParamType.REQUEST_BODY, parameterName);
    }

    private static ParamType getParamType(Annotation paramAnnotation) {
        if (!isParamAnnotation(paramAnnotation)) {
            throw new IllegalStateException("internal error - trying to get http method from annotation of type %s".formatted(paramAnnotation.getClass().getName()));
        }
        return paramAnnotation.annotationType().getAnnotation(ParamAnnotation.class).value();
    }
}
