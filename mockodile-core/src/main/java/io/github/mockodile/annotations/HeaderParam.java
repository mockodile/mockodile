package io.github.mockodile.annotations;

import io.github.mockodile.domain.ParamType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@ParamAnnotation(ParamType.HEADER)
public @interface HeaderParam {
    String value() default "";
}
