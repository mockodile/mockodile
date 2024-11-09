package io.github.mockodile;

import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.RequestCount;

import java.lang.reflect.Proxy;

/**
 * The main API for interacting with Mockodile from within a test.
 * <br/>
 * To create a mock use {@link #mock(Class)} providing a interface class which has been annotated with {@link MockedApi}.
 * <br/>
 * To mock some behaviour use {@link #when(Object)} or {@link #when(Runnable)} calling the relevant method of the MockedApi
 * between the brackets. Examples include:
 * <code>
 *     mockApi.when(myMockedApi.getById("id")).willReturn("myResponse");
 *     mockApi.when(() -> myMockedApi.post("id")).willReturn(200, headers);
 * </code>
 * <br/>
 * To verify behavior use one of the verify methods, for example {@link #verify(Object)} or {@link #verify(Runnable)}.
 * Examples include:
 * <code>
 *     mockApi.verify(myMockedApi.getById("id"));
 *     mockApi.when(() -> myMockedApi.post("id"));
 * </code>
 */
public class MockApi {

    private final Mocker mocker;

    public MockApi(Mocker mocker) {
        this.mocker = mocker;
    }

    /**
     * Create a mock from the interface of the target class.
     * The provided class must be an interface, must be annotated with {@link MockedApi}
     * and must have correctly annotated methods.
     *
     * @param target the MockedApi interface to be mocked.
     * @return a mock object which can be used to define and verify wiremock requests
     * @param <T> the type of the mock created
     */
    @SuppressWarnings("unchecked")
    public <T> T mock(Class<T> target) {

        if (!target.isInterface()) {
            throw new IllegalArgumentException("MockApi only works with interfaces but class %s was provided".formatted(target.getName()));
        }

        if (!target.isAnnotationPresent(MockedApi.class)) {
            throw new IllegalArgumentException("MockApi clients must be annotated with @MockedApi but provided inteface %s is not annotated".formatted(target.getName()));
        }

        return (T) Proxy.newProxyInstance(
                target.getClassLoader(),
                new Class<?>[]{target},
                new MockedApiInvocationHandler<>(target));
    }

    /**
     * Used for methods with a return type.
     * for example for the method definition <code>MyReturnType myMethod(String id)</code> the mapping can be defined as:
     * <code>mockApi.when(myApi.myMethod("id1")).willReturn(instanceOfMyReturnType);</code>
     *
     * @param any not used, only present for fluent api design
     * @param <T> response type
     * @return OngoingStubbing to allow specification of response
     */
    public <T> OngoingStubbing<T> when(@SuppressWarnings("unused") T any) {
        return ongoingStubbing();
    }

    /**
     * used for void methods without a return type
     *
     * <pre>mockApi.when(() -> myApi.anyVoidMethod(param1)).willReturn(...);</pre>
     * <p>
     * Note the content of the runnable cannot be checked but should simply be a call
     * to wiremock client method.
     *
     * @param runnable call the void method
     * @return OngoingStubbing to allow specification of response
     */
    public VoidOngoingStubbing when(Runnable runnable) {
        runnable.run();
        return new VoidOngoingStubbing(ongoingStubbing());
    }

    private <T> OngoingStubbing<T> ongoingStubbing() {
        try {
            Invocation invocation = ThreadSafeMockingState.mockingState().invocation()
                    .orElseThrow(() -> new IllegalStateException("when methods of a @MockedApi must be used as follows mockApi.when( mockedApi.anyMethod() ).willReturn()"));
            return new OngoingStubbing<>(mocker, invocation);
        } finally {
            ThreadSafeMockingState.clear();
        }
    }

    /**
     * Verify that the mocked api void method called by the runnable was not performed.
     * The runnable must be a single call to a MockedApi such as
     * <code>verifyNone(() -> myMockedApi.updateOrder(1))</code>
     * If the mocked api method is not void use {@link #verifyNone(Object)} to avoid the need
     * for an un-necessary runnable.
     * <br/>
     * Raises an exception if the mocked api was performed.
     * @param runnable the call to the mockedApi typically a void method.
     */
    public void verifyNone(Runnable runnable) {
        runnable.run();
        verifyInternal(RequestCount.exactly(0));
    }

    /**
     * Verify that a mocked api was not performed.
     * The verifyNone method should be used as follows
     * <code>verifyNone(myMockedApi.getOrderById(1))</code>.
     * <br/>
     * Raises an exception if the mocked api was performed.
     *
     * @param any not used - the result of calling mockedApi method.
     * @param <T> the response type of the mocked methodApi method.
     */
    public <T> void verifyNone(@SuppressWarnings("unused") T any) {
        verifyInternal(RequestCount.exactly(0));
    }

    /**
     * Verify that the mocked api void method called by the runnable was performed at least once.
     * The runnable must be a single call to a MockedApi such as
     * <code>verify(() -> myMockedApi.updateOrder(1))</code>
     * <br/>
     * If the mocked api method is not void use {@link #verify(Object)} to avoid the need
     * for an un-necessary runnable.
     * <br/>
     * Raises an exception if the mocked api was not performed at least once.
     *
     * @param runnable the call to the mockedApi typically a void method.
     */
    public OngoingVerification verify(Runnable runnable) {
        runnable.run();
        return verify((Void) null);
    }

    /**
     * Verify that a mocked api was performed at least once.
     * The verify method should be used as follows
     * <code>verify(myMockedApi.getOrderById(1))</code>.
     * <br/>
     * If the mocked method is void use {@link #verify(Runnable)}.
     * <br/>
     * Raises an exception if the mocked api not was performed at least once.
     *
     * @param any not used - the result of calling mockedApi method.
     * @param <T> the response type of the mocked methodApi method.
     */
    public <T> OngoingVerification verify(@SuppressWarnings("unused") T any) {
        return verifyInternal(RequestCount.greaterThanOrEqualTo(1));
    }

    /**
     * Verify that the mocked api void method called by the runnable was performed <i>count</i> times.
     * The runnable must be a single call to a MockedApi such as
     * <code>verify(2, () -> myMockedApi.updateOrder(1))</code>
     * <br/>
     * If the mocked api method is not void use {@link #verify(int, Object)} to avoid the need
     * for an un-necessary runnable.
     * <br/>
     * Raises an exception if the mocked api was not the specified number of times.
     *
     * @param count the expected number of times the api should have been called.
     * @param runnable the call to the mockedApi typically a void method.
     */
    public OngoingVerification verify(int count, Runnable runnable) {
        runnable.run();
        return verify(count, (Void) null);
    }

    /**
     * Verify that the mocked api was performed <i>count</i> times.
     * The verify method should be used as follows
     * <code>verify(2, myMockedApi.getOrderById(1))</code>
     * <br/>
     * If the mocked method is void use {@link #verify(int, Runnable)}.
     * <br/>
     * Raises an exception if the mocked api was not the specified number of times.
     *
     * @param count the expected number of times the api should have been called.
     * @param any not used - the result of calling mockedApi method.
     */
    public <T> OngoingVerification verify(int count, @SuppressWarnings("unused") T any) {
        return verifyInternal(RequestCount.exactly(count));
    }


    /**
     * Verify that the mocked api void method called by the runnable was performed the number of times represented by <i>count</i>.
     * The runnable must be a single call to a MockedApi such as
     * <code>verify(WireMock.moreThan(2), () -> myMockedApi.updateOrder(1))</code>
     * <br/>
     * If the mocked api method is not void use {@link #verify(RequestCount, Object)} to avoid the need
     * for an un-necessary runnable.
     * <br/>
     * Raises an exception if the mocked api was not the specified number of times.
     *
     * @param count the count matching strategy to be applied.
     * @param runnable the call to the mockedApi typically a void method.
     */
    public OngoingVerification verify(RequestCount count, Runnable runnable) {
        runnable.run();
        return verify(count, (Void) null);
    }

    /**
     * Verify that the mocked api was performed the number of times represented by <i>count</i>.
     * The verify method should be used as follows
     * <code>verify(2, myMockedApi.getOrderById(1))</code>
     * <br/>
     * If the mocked method is void use {@link #verify(RequestCount, Runnable)}.
     * <br/>
     * Raises an exception if the mocked api was not the specified number of times.
     *
     * @param count the expected number of times the api should have been called.
     * @param any not used - the result of calling mockedApi method.
     */
    public <T> OngoingVerification verify(RequestCount count, @SuppressWarnings("unused") T any) {
        return verifyInternal(count);
    }

    private OngoingVerification verifyInternal(RequestCount requestCount) {
        try {
            Invocation invocation = ThreadSafeMockingState.mockingState().invocation()
                    .orElseThrow(() -> new IllegalStateException("verify methods of declarative wiremock must be used as follows mockApi.verify( mockedApi.anyMethod() )"));

            return mocker.verify(requestCount, invocation);
        } finally {
            ThreadSafeMockingState.clear();
        }
    }

    void reset() {
        mocker.reset();
    }
}
