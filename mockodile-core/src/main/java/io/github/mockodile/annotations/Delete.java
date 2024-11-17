package io.github.mockodile.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.mockodile.domain.HttpMethod.DELETE;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@RequestMethodAnnotation(DELETE)
public @interface Delete {
    String path() default "";

    String[] headers() default {};

    String[] jsonPath() default {};
}
