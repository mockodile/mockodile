package io.github.mockodile.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.mockodile.domain.HttpMethod.GET;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@RequestMethodAnnotation(GET)
public @interface Get {
    String path() default "";

    String[] headers() default {};

    String[] jsonPath() default {};
}
