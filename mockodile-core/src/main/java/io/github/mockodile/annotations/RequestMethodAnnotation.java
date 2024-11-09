package io.github.mockodile.annotations;

import io.github.mockodile.domain.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@interface RequestMethodAnnotation {
    HttpMethod value();
}

