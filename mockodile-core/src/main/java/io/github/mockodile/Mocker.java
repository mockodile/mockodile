package io.github.mockodile;

import io.github.mockodile.domain.*;

import java.util.List;

public interface Mocker {

    void register(Invocation invocation, Response<?> response);

    OngoingVerification verify(RequestCount requestCount, Invocation invocation);

    List<Request<BodyAsString>> findRequests(Invocation invocation);

    <T> List<Request<T>> findRequests(Invocation invocation, Class<T> bodyType);

    void reset();
}
