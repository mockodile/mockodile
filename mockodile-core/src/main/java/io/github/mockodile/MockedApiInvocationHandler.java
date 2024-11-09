package io.github.mockodile;

import io.github.mockodile.argumentmatcher.ArgumentMatcher;
import io.github.mockodile.argumentmatcher.EqualityArgumentMatcher;
import io.github.mockodile.domain.AnnotationData;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.utils.CollectionUtils;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MockedApiInvocationHandler<T> implements InvocationHandler {

    private final Class<T> type;
    private final AnnotationMapper annotationMapper = new DefaultAnnotationMapper();

    private final Map<Method, AnnotationData> map;

    public MockedApiInvocationHandler(Class<T> type) {
        this.type = type;
        this.map = Stream.of(type.getDeclaredMethods())
                .filter(method -> annotationMapper.isSupported(type, method))
                .collect(Collectors.toMap(Function.identity(), method -> annotationMapper.from(type, method)));

        if (map.isEmpty()) {
            throw new IllegalArgumentException("MockedApi of type %s does not have any annotated request methods".formatted(type.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass().equals(Object.class)) {
            return handleJavaLangObjectMethod(proxy, method, args);
        }

        try {
            var invocation = toInvocation(method, args);
            ThreadSafeMockingState.mockingState().registerInvocation(invocation);
            return getReturnTypeWithSafePrimitives(method);
        } catch (RuntimeException e) {
            ThreadSafeMockingState.clear();
            throw e;
        }
    }

    private Invocation toInvocation(Method method, Object[] args) {
        List<ArgumentMatcher> argumentMatchers = ThreadSafeMockingState.mockingState().argumentMatchers();

        if (CollectionUtils.isEmpty(argumentMatchers)) {
            argumentMatchers = args == null ?
                    List.of() :
                    Stream.of(args)
                            .map(MockedApiInvocationHandler::eq)
                            .toList();
        }

        if (!map.containsKey(method)) {
            throw new IllegalArgumentException("method %s of %s is not mapped to an annotation data and is not supported"
                    .formatted(method.toGenericString(), type.getName()));
        }

        AnnotationData annotationData = map.get(method);

        return new Invocation(method, annotationData, argumentMatchers);
    }

    private static Object getReturnTypeWithSafePrimitives(Method method) {
        if (method.getReturnType().isPrimitive() && method.getReturnType() != void.class) {
            // for primitives this will get a reasonable default to avoid autoboxing issues when returning null
            return Array.get(Array.newInstance(method.getReturnType(), 1), 0);
        }
        return null;
    }

    private Object handleJavaLangObjectMethod(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            try {
                return "MockedApi proxy for interface [%s] : [%s]".formatted(type.getName(), method.invoke(Proxy.getInvocationHandler(proxy), args));
            } catch (IllegalAccessException | InvocationTargetException e) {
                return "MockedApi proxy for interface [%s] : unexpected error evaluating toString method [%s]".formatted(type.getName(), e.toString());
            }
        }
        try {
            return method.invoke(Proxy.getInvocationHandler(proxy), args);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            } else {
                @SuppressWarnings("java:1122") RuntimeException runtimeException = new RuntimeException(e);
                throw runtimeException;
            }
        } catch (IllegalAccessException e) {
            @SuppressWarnings("java:1122") RuntimeException runtimeException = new RuntimeException(e);
            throw runtimeException;
        }
    }

    private static ArgumentMatcher eq(Object o) {
        return new EqualityArgumentMatcher<>(o);
    }
}
