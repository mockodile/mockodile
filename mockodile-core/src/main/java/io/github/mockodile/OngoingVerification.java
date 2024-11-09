package io.github.mockodile;

import io.github.mockodile.domain.BodyAsString;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.Request;

import java.util.List;

/**
 * Allows for additional validation of actual requests.
 * Returned by verify methods such as {@link MockApi#verify(Object)} {@link MockApi#verify(Runnable)} to allow clients to verify actual requests using a fluid api.
 * <br/>
 * Example use:
 * <code>mockApi.verify(myApi.anyMethod()).andGetLoggedRequest()</code>
 */
public class OngoingVerification {

    private final Mocker mocker;
    private final Invocation invocation;

    public OngoingVerification(Mocker mocker, Invocation invocation) {
        this.mocker = mocker;
        this.invocation = invocation;
    }

    public Request<BodyAsString> andGetRequest() {
        return mocker.findRequests(invocation).get(0);
    }

    public <T> Request<T> andGetRequest(Class<T> bodyType) {
        return mocker.findRequests(invocation, bodyType).get(0);
    }

    public List<Request<BodyAsString>> andGetRequests() {
        return mocker.findRequests(invocation);
    }

    public <T> List<Request<T>> andGetRequests(Class<T> bodyType) {
        return mocker.findRequests(invocation, bodyType);
    }

    public String andGetRequestBodyAsString() {
        return andGetRequest().body().value();
    }

    public <T> T andGetRequestBody(Class<T> clazz) {
        return andGetRequest(clazz).body();
    }
}
