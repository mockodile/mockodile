package io.github.mockodile.annotations;

import io.github.mockodile.domain.HttpMethod;
import io.github.mockodile.domain.RequestAnnotationDataBuilder;
import io.github.mockodile.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.util.List;

public final class RequestMethodAnnotationUtil {
    private RequestMethodAnnotationUtil() {
        // do not construct
    }

    public static boolean isRequestMethodAnnotation(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(RequestMethodAnnotation.class);
    }

    public static void applyAnnotationTo(Annotation requestAnnotation, RequestAnnotationDataBuilder builder) {
        switch (getHttpMethod(requestAnnotation)) {
            case GET -> applyGet((Get) requestAnnotation, builder);
            case HEAD -> applyHead((Head) requestAnnotation, builder);
            case POST -> applyPost((Post) requestAnnotation, builder);
            case PUT -> applyPut((Put) requestAnnotation, builder);
            case PATCH -> applyPatch((Patch) requestAnnotation, builder);
            case DELETE -> applyDelete((Delete) requestAnnotation, builder);
            case OPTIONS -> applyOptions((Options) requestAnnotation, builder);
            case TRACE -> applyTrace((Trace) requestAnnotation, builder);
        }
    }

    private static void applyGet(Get get, RequestAnnotationDataBuilder builder) {
        builder.withHttpMethod(HttpMethod.GET);
        apply(get.path(), get.headers(), builder);
    }

    private static void applyHead(Head head, RequestAnnotationDataBuilder builder) {
        builder.withHttpMethod(HttpMethod.HEAD);
        apply(head.path(), head.headers(), builder);
    }

    private static void applyPost(Post post, RequestAnnotationDataBuilder builder) {
        builder.withHttpMethod(HttpMethod.POST);
        apply(post.path(), post.headers(), builder);
    }

    private static void applyPut(Put put, RequestAnnotationDataBuilder builder) {
        builder.withHttpMethod(HttpMethod.PUT);
        apply(put.path(), put.headers(), builder);
    }

    private static void applyPatch(Patch patch, RequestAnnotationDataBuilder builder) {
        builder.withHttpMethod(HttpMethod.PATCH);
        apply(patch.path(), patch.headers(), builder);
    }

    private static void applyDelete(Delete delete, RequestAnnotationDataBuilder builder) {
        builder.withHttpMethod(HttpMethod.DELETE);
        apply(delete.path(), delete.headers(), builder);
    }

    private static void applyOptions(Options options, RequestAnnotationDataBuilder builder) {
        builder.withHttpMethod(HttpMethod.OPTIONS);
        apply(options.path(), options.headers(), builder);
    }

    private static void applyTrace(Trace trace, RequestAnnotationDataBuilder builder) {
        builder.withHttpMethod(HttpMethod.TRACE);
        apply(trace.path(), trace.headers(), builder);
    }

    private static void apply(String path, String[] headers, RequestAnnotationDataBuilder builder) {
        if (StringUtils.hasText(path)) {
            builder.withPath(path);
        }

        if (headers != null) {
            builder.withHeaders(List.of(headers));
        }
    }

    private static HttpMethod getHttpMethod(Annotation requestAnnotation) {
        if (!isRequestMethodAnnotation(requestAnnotation)) {
            throw new IllegalStateException("internal error - trying to get http method from annotation of type %s".formatted(requestAnnotation.getClass().getName()));
        }
        return requestAnnotation.annotationType().getAnnotation(RequestMethodAnnotation.class).value();
    }
}
