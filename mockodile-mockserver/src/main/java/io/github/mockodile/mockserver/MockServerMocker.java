package io.github.mockodile.mockserver;

import io.github.mockodile.Mocker;
import io.github.mockodile.ObjectMapper;
import io.github.mockodile.OngoingVerification;
import io.github.mockodile.domain.*;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.NottableString;
import org.mockserver.verify.VerificationTimes;

import java.util.List;
import java.util.stream.Stream;

public class MockServerMocker implements Mocker {

    private final MockServerClientProvider mockServerClientProvider;
    private final ObjectMapper objectMapper;

    public MockServerMocker(MockServerClientProvider mockServerClientProvider, ObjectMapper objectMapper) {
        this.mockServerClientProvider = mockServerClientProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void register(Invocation invocation, Response<?> response) {
        mockServerClientProvider.getMockServerClient()
                .when(new RequestBuilder(objectMapper, invocation).build())
                .respond(new ResponseBuilder<>(objectMapper, response).build());
    }

    @Override
    public OngoingVerification verify(RequestCount requestCount, Invocation invocation) {
        mockServerClientProvider.getMockServerClient()
                .verify(new RequestBuilder(objectMapper, invocation).build(), mapToVerificationTimes(requestCount));
        return new OngoingVerification(this, invocation);
    }

    private VerificationTimes mapToVerificationTimes(RequestCount requestCount) {
        return switch (requestCount.getRequestCountType()) {
            case EXACTLY -> VerificationTimes.exactly(requestCount.getValue());
            case LESS_THAN -> VerificationTimes.atMost(requestCount.getValue() - 1);
            case GREATER_THAN -> VerificationTimes.atLeast(requestCount.getValue() + 1);
            case LESS_THAN_OR_EQUAL -> VerificationTimes.atMost(requestCount.getValue());
            case GREATER_THAN_OR_EQUAL -> VerificationTimes.atLeast(requestCount.getValue());
        };
    }

    @Override
    public List<Request<BodyAsString>> findRequests(Invocation invocation) {
        var recordedRequests = getRecordedRequests(invocation);
        return Stream.of(recordedRequests).map(MockServerMocker::toRequest).toList();
    }

    @Override
    public <T> List<Request<T>> findRequests(Invocation invocation, Class<T> bodyType) {
        var recordedRequests = getRecordedRequests(invocation);
        return Stream.of(recordedRequests).map(r -> toRequest(r, bodyType)).toList();
    }

    private HttpRequest[] getRecordedRequests(Invocation invocation) {
        return mockServerClientProvider.getMockServerClient()
                .retrieveRecordedRequests(new RequestBuilder(objectMapper, invocation).build());
    }

    private static Request<BodyAsString> toRequest(HttpRequest httpRequest) {
        var bodyAsString = httpRequest.getBodyAsString();
        return toRequest(httpRequest, new BodyAsString(bodyAsString));
    }

    private <T> Request<T> toRequest(HttpRequest httpRequest, Class<T> bodyType) {
        var bodyAsString = httpRequest.getBodyAsString();
        T body = null;
        if (bodyAsString != null) {
            body = objectMapper.readValue(bodyAsString, bodyType);
        }
        return toRequest(httpRequest, body);

    }

    private static <T> Request<T> toRequest(HttpRequest httpRequest, T body) {
        Request.RequestBuilder<T> builder = Request.builder();
        builder = builder.withPath(httpRequest.getPath().getValue());
        builder = builder.withBody(body);

        var queryParamsBuilder = QueryParams.builder();
        if (httpRequest.getQueryStringParameters() != null) {
            httpRequest.getQueryStringParameters()
                    .getEntries()
                    .forEach(qp -> queryParamsBuilder.put(
                            qp.getName().getValue(),
                            qp.getValues().stream().map(NottableString::getValue).toList()));
        }
        builder.withQueryParams(queryParamsBuilder.build());

        var headersBuilder = HttpHeaders.builder();
        if (httpRequest.getHeaders() != null) {
            httpRequest.getHeaders()
                    .getEntries()
                    .forEach(h -> headersBuilder.put(
                            h.getName().getValue(),
                            h.getValues().stream().map(NottableString::getValue).toList()));
        }
        builder.withHeaders(headersBuilder.build());

        return builder.build();
    }

    @Override
    public void reset() {
        mockServerClientProvider.getMockServerClient()
                .reset();
    }
}
