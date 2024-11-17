package io.github.mockodile.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.mockodile.domain.HttpMethod.HEAD;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@RequestMethodAnnotation(HEAD)
public @interface Head {
    String path() default "";

    String[] headers() default {};

    String[] jsonPath() default {};
}
