package io.github.mockodile.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.mockodile.domain.HttpMethod.PUT;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@RequestMethodAnnotation(PUT)
public @interface Put {
    String path() default "";

    String[] headers() default {};

    String[] jsonPath() default {};
}
