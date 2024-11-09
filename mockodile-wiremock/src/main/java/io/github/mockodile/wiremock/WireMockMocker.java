package io.github.mockodile.wiremock;

import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.github.mockodile.Mocker;
import io.github.mockodile.ObjectMapper;
import io.github.mockodile.OngoingVerification;
import io.github.mockodile.domain.*;

import java.util.List;

public class WireMockMocker implements Mocker {

    private final WireMock wireMock;
    private final ObjectMapper objectMapper;

    public WireMockMocker(WireMock wireMock, ObjectMapper objectMapper) {
        this.wireMock = wireMock;
        this.objectMapper = objectMapper;
    }

    @Override
    public void register(Invocation invocation, Response<?> response) {
        wireMock.register(
                new RequestBuilder(objectMapper, invocation).toMappingBuilder()
                        .willReturn(new ResponseBuilder<>(objectMapper, response).build()));
    }

    @Override
    public OngoingVerification verify(RequestCount requestCount, Invocation invocation) {
        RequestBuilder requestBuilder = new RequestBuilder(objectMapper, invocation);

        var requestPatternBuilder = requestBuilder.toRequestPatternBuilder();
        wireMock.verifyThat(toCountMatchingStrategy(requestCount), requestPatternBuilder);
        return new OngoingVerification(this, invocation);
    }

    private static CountMatchingStrategy toCountMatchingStrategy(RequestCount requestCount) {
        return switch (requestCount.getRequestCountType()) {
            case EXACTLY -> WireMock.exactly(requestCount.getValue());
            case LESS_THAN -> WireMock.lessThan(requestCount.getValue());
            case GREATER_THAN -> WireMock.moreThan(requestCount.getValue());
            case LESS_THAN_OR_EQUAL -> WireMock.lessThanOrExactly(requestCount.getValue());
            case GREATER_THAN_OR_EQUAL -> WireMock.moreThanOrExactly(requestCount.getValue());
        };
    }

    @Override
    public List<Request<BodyAsString>> findRequests(Invocation invocation) {
        var loggedRequests = getLoggedRequests(new RequestBuilder(objectMapper, invocation));
        return loggedRequests.stream().map(WireMockMocker::toRequest).toList();
    }

    @Override
    public <T> List<Request<T>> findRequests(Invocation invocation, Class<T> bodyType) {
        var loggedRequests = getLoggedRequests(new RequestBuilder(objectMapper, invocation));
        return loggedRequests.stream().map(s -> toRequest(s, bodyType)).toList();
    }

    private List<LoggedRequest> getLoggedRequests(RequestBuilder requestBuilder) {
        return wireMock.find(requestBuilder.toRequestPatternBuilder());
    }

    private static Request<BodyAsString> toRequest(LoggedRequest loggedRequest) {
        var bodyAsString = loggedRequest.getBodyAsString();
        return toRequest(loggedRequest, new BodyAsString(bodyAsString));
    }

    private <T> Request<T> toRequest(LoggedRequest loggedRequest, Class<T> bodyType) {
        var bodyAsString = loggedRequest.getBodyAsString();
        T body = null;
        if (bodyAsString != null) {
            body = objectMapper.readValue(bodyAsString, bodyType);
        }
        return toRequest(loggedRequest, body);
    }

    private static <T> Request<T> toRequest(LoggedRequest loggedRequest, T body) {
        var splitPath = loggedRequest.getUrl().split("\\?", 2);
        Request.RequestBuilder<T> builder = Request.builder();
        builder = builder.withPath(splitPath[0]);
        builder = builder.withBody(body);
        if (splitPath.length == 2) {
            builder.withQueryParams(QueryParams.from(splitPath[1]));
        }

        var headersBuilder = HttpHeaders.builder();
        loggedRequest.getHeaders().all().forEach(h -> headersBuilder.put(h.key(), h.values()));
        builder.withHeaders(headersBuilder.build());

        return builder.build();
    }

    @Override
    public void reset() {
        // no reset required for wiremock at present
    }
}
