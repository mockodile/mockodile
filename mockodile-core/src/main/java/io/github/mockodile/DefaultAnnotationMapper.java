package io.github.mockodile;

import io.github.mockodile.annotations.ParamAnnotationUtil;
import io.github.mockodile.annotations.RequestMapping;
import io.github.mockodile.annotations.RequestMethodAnnotationUtil;
import io.github.mockodile.annotations.Response;
import io.github.mockodile.domain.AnnotationData;
import io.github.mockodile.domain.AnnotationDataBuilder;
import io.github.mockodile.domain.HttpHeaders;
import io.github.mockodile.utils.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Default annotation mapper which supports the annotations provided in package {@link io.github.mockodile.annotations}.
 * </p>
 * Other libraries may wish to provide their own mapping from annotations such as for RequestMapping from spring.
 */
public class DefaultAnnotationMapper implements AnnotationMapper {

    @Override
    public <T> boolean isSupported(Class<T> type, Method method) {
        return Arrays.stream(method.getAnnotations())
                .anyMatch(RequestMethodAnnotationUtil::isRequestMethodAnnotation);
    }

    @Override
    public <T> AnnotationData from(Class<T> type, Method method) {
        var builder = AnnotationData.builder();

        applyTypeAnnotation(type, builder);
        applyMethodAnnotation(method, builder);
        applyParameterAnnotations(method, builder);

        return builder.build();
    }

    private <T> void applyTypeAnnotation(Class<T> type, AnnotationDataBuilder builder) {
        if (type.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = type.getAnnotation(RequestMapping.class);

            if (StringUtils.hasText(requestMapping.path())) {
                builder.requestAnnotationDataBuilder().withPath(requestMapping.path());
            }

            if (requestMapping.headers() != null) {
                HttpHeaders headers = HttpHeaders.builder().addAll(List.of(requestMapping.headers())).build();
                headers.toMap().forEach((k, v) -> builder.requestAnnotationDataBuilder().withHeader(k, v));
            }

            if (requestMapping.responseHeaders() != null) {
                HttpHeaders headers = HttpHeaders.builder().addAll(List.of(requestMapping.responseHeaders())).build();
                headers.toMap().forEach((k, v) -> builder.responseAnnotationDataBuilder().withHeader(k, v));
            }
        }
    }

    private void applyMethodAnnotation(Method method, AnnotationDataBuilder builder) {
        var foundRequestMethodAnnotations = Arrays.stream(method.getAnnotations())
                .filter(RequestMethodAnnotationUtil::isRequestMethodAnnotation)
                .toList();

        if (foundRequestMethodAnnotations.isEmpty()) {
            throw new IllegalStateException("found no request method annotations on the method %s of class %s"
                    .formatted(method.toGenericString(), method.getDeclaringClass().getName()));
        }

        if (foundRequestMethodAnnotations.size() > 1) {
            throw new IllegalStateException("found %s request method annotations on the method %s of class %s"
                    .formatted(foundRequestMethodAnnotations.size(), method.toGenericString(), method.getDeclaringClass().getName()));
        }

        RequestMethodAnnotationUtil.applyAnnotationTo(foundRequestMethodAnnotations.get(0), builder.requestAnnotationDataBuilder());

        // optional Response annotation
        if (method.isAnnotationPresent(Response.class)) {
            Response response = method.getAnnotation(Response.class);
            builder.responseAnnotationDataBuilder().withStatus(response.status());

            if (response.headers() != null) {
                HttpHeaders headers = HttpHeaders.builder().addAll(List.of(response.headers())).build();
                headers.toMap().forEach((k, v) -> builder.responseAnnotationDataBuilder().withHeader(k, v));
            }
        }

    }

    private void applyParameterAnnotations(Method method, AnnotationDataBuilder builder) {
        Stream.of(method.getParameters()).forEach(p -> applyParameterAnnotation(method, p, builder));
    }

    private void applyParameterAnnotation(Method method, Parameter parameter, AnnotationDataBuilder builder) {
        var foundParamAnnotations = Stream.of(parameter.getAnnotations()).filter(ParamAnnotationUtil::isParamAnnotation).toList();

        if (foundParamAnnotations.isEmpty()) {
            throw new IllegalStateException("found no parameter annotations on param %s of the method %s of class %s"
                    .formatted(parameter.getName(), method.toGenericString(), method.getDeclaringClass().getName()));
        }

        if (foundParamAnnotations.size() > 1) {
            throw new IllegalStateException("found %s parameter annotations on param %s of the method %s of class %s"
                    .formatted(foundParamAnnotations.size(), parameter.getName(), method.toGenericString(), method.getDeclaringClass().getName()));
        }

        ParamAnnotationUtil.applyAnnotationTo(foundParamAnnotations.get(0), parameter.getName(), builder.requestAnnotationDataBuilder());
    }
}
