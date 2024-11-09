package io.github.mockodile;

import io.github.mockodile.domain.AnnotationData;

import java.lang.reflect.Method;

public interface AnnotationMapper {

    <T> boolean isSupported(Class<T> type, Method method);
    <T> AnnotationData from(Class<T> type, Method method);
}
