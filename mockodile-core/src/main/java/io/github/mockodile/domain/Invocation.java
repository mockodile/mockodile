package io.github.mockodile.domain;

import io.github.mockodile.argumentmatcher.ArgumentMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public record Invocation(
        Method method,
        AnnotationData annotationData,
        List<ArgumentMatcher> argumentMatchers
) {

    public Invocation {
        if (method == null || argumentMatchers == null || annotationData.requestAnnotationData() == null) {
            throw new IllegalArgumentException("argument matcher missing parameters");
        }

        if (argumentMatchers.size() != method.getParameters().length) {
            throw new IllegalArgumentException("""
                    argumentMatchers length %s does not match method parameter length.%s%n
                    When using argument matchers with a declarative wiremock client either all parameters must be specified with argument matchers or none
                    Correct usage example with no argument matchers: declarativeWiremock.when(myMockedApi.get(id1, id2).willReturn(...)
                    Correct usage example with argument matchers:    declarativeWiremock.when(myMockedApi.get(anyPath(), eq(id2)).willReturn(...)
                    Incorrect usage:                                 declarativeWiremock.when(myMockedApi.get(anyPath(), eq(id2)).willReturn(...)")"""
                    .formatted(
                            argumentMatchers.size(),
                            method.getParameters().length));
        }

        if (annotationData.requestAnnotationData().paramAnnotationData().size() != method.getParameters().length) {
            throw new IllegalArgumentException("annotated parameter length %n does not match method parameter length %n.".formatted());
        }
    }

    public List<InvokedParameter> getParameters() {
        List<InvokedParameter> result = new ArrayList<>();
        for (int i = 0; i < method.getParameters().length; i++) {
            result.add(new InvokedParameter(method.getParameters()[i], annotationData.requestAnnotationData().paramAnnotationData().get(i), argumentMatchers.get(i)));
        }
        return result;
    }

    public record InvokedParameter(Parameter parameter,
                                   ParamAnnotationData paramAnnotationData,
                                   ArgumentMatcher argumentMatcher) {
    }

}
