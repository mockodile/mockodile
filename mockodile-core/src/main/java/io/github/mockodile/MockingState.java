package io.github.mockodile;

import io.github.mockodile.argumentmatcher.ArgumentMatcher;
import io.github.mockodile.domain.Invocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class MockingState {

    private final List<ArgumentMatcher> argumentMatchers = new ArrayList<>();
    private Invocation invocation;
    private Exception lastInvocationStack;

    void registerInvocation(Invocation invocation) {
        if (this.invocation != null) {

            // clear so that any other tests still have a chance of passing
            ThreadSafeMockingState.clear();

            throw new IllegalArgumentException(("Incorrect usage of @MockedApi, calls to mockApi.when(...) " +
                    "must be completed with a willReturn() call, i.e. mockApi.when(myApi.anyRequest()).willReturn(...)%n" +
                    "last invocation for method %s%n" +
                    "see cause in stacktrace").formatted(this.invocation.method().toGenericString()),
                    this.lastInvocationStack);
        }
        this.invocation = invocation;
        this.lastInvocationStack = new Exception("IncorrectUsage: %s".formatted(invocation.method().toGenericString()));
    }

    void registerArgumentMatcher(ArgumentMatcher argumentMatcher) {
        argumentMatchers.add(argumentMatcher);
    }

    Optional<Invocation> invocation() {
        return Optional.ofNullable(invocation);
    }

    List<ArgumentMatcher> argumentMatchers() {
        return argumentMatchers;
    }

}
