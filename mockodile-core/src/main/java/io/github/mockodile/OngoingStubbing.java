package io.github.mockodile;

import io.github.mockodile.annotations.HeaderParam;
import io.github.mockodile.domain.HttpHeaders;
import io.github.mockodile.domain.Invocation;
import io.github.mockodile.domain.Response;

/**
 * Allows for specification of responses for non-void mocked apis.
 * Returned by {@link MockApi#when(Object)} to allow clients to specify responses using a fluid api.
 * <br/>
 * Example use:
 * <code>mockApi.when(myApi.anyMethod()).willReturn(...)</code>
 *
 * @param <T> the return body type of the mocked method.
 */
public class OngoingStubbing<T> {
    private final Mocker mocker;
    private final Invocation invocation;

    OngoingStubbing(Mocker mocker, Invocation invocation) {
        this.mocker = mocker;
        this.invocation = invocation;
    }

    /**
     * Specify the return body, the body will be mapped to json using the object mapper.
     * The status will be the status specified on the method of the @MockedApi method with the annotation.
     * {@link Response#status()} or 200 by default.
     * The headers will include any headers specified on the @MockedApi method with
     * {@link HeaderParam}.
     *
     * @param body the return value
     *
     * @deprecated use then return
     */
    @Deprecated(since = "0.0.4")
    public void willReturn(T body) {
        thenReturn(body);
    }

    /**
     * Specify the return body, the body will be mapped to json using the object mapper.
     * The status will be the status specified on the method of the @MockedApi method with the annotation.
     * {@link Response#status()} or 200 by default.
     * The headers will include any headers specified on the @MockedApi method with
     * {@link HeaderParam}.
     *
     * @param body the return value
     */
    public void thenReturn(T body) {
        @SuppressWarnings("unchecked")
        Response.ResponseBuilder<T> responseBuilder = (Response.ResponseBuilder<T>) Response.builder()
                .withStatus(invocation.annotationData().responseAnnotationData().defaultStatus())
                .withHeaders(invocation.annotationData().responseAnnotationData().headers());

        if (body != null) {
            responseBuilder.withBody(body);
        }

        thenRespond(responseBuilder.build());
    }

    /**
     * Specify the complete http response including status, body and headers.
     * The status param will overwrite any annotated @Response.status() annotation.
     * The body will be serialized to json using the object mapper.
     * The headers provided will be combined with any headers specified on the @MockedApi.
     *
     * @param status the status of the http response
     * @param body   the body of the http response
     *
     * @deprecated use thenRespond
     */
    @Deprecated(since = "0.0.4")
    public void willReturn(int status, T body, HttpHeaders headers) {
        thenRespond(status, body, headers);
    }

    /**
     * Specify the complete http response including status, body and headers.
     * The status param will overwrite any annotated @Response.status() annotation.
     * The body will be serialized to json using the object mapper.
     * The headers provided will only contain headers specified on the @MockedApi.
     *
     * @param status the status of the http response
     * @param body   the body of the http response
     */
    public void thenRespond(int status, T body) {
        thenRespond(status, body, HttpHeaders.empty());
    }

    /**
     * Specify the complete http response including status, body and headers.
     * The status param will overwrite any annotated @Response.status() annotation.
     * The body will be serialized to json using the object mapper.
     * The headers provided will be combined with any headers specified on the @MockedApi.
     *
     * @param status the status of the http response
     * @param body   the body of the http response
     */
    public void thenRespond(int status, T body, HttpHeaders headers) {
        @SuppressWarnings("unchecked")
        Response.ResponseBuilder<T> responseBuilder = (Response.ResponseBuilder<T>) Response.builder()
                .withStatus(status)
                .withHeaders(invocation.annotationData().responseAnnotationData().headers())
                .withHeaders(headers);

        if (body != null) {
            responseBuilder.withBody(body);
        }
        thenRespond(responseBuilder.build());
    }

    /**
     * Specify the status of the http response.
     * The status param will overwrite any annotated @Response.status() annotation.
     * No body will be returned.
     * The headers provided will only contain headers specified on the @MockedApi.
     *
     * @param status the status of the http response
     *
     * @deprecated use thenReturnStatus
     */
    @Deprecated(since = "0.0.4")
    public void willReturn(int status) {
        thenReturnStatus(status);
    }

    /**
     * Specify the status of the http response.
     * The status param will overwrite any annotated @Response.status() annotation.
     * No body will be returned.
     * The headers provided will only contain headers specified on the @MockedApi.
     *
     * @param status the status of the http response
     */
    public void thenReturnStatus(int status) {
        @SuppressWarnings("unchecked")
        Response.ResponseBuilder<T> responseBuilder = (Response.ResponseBuilder<T>) Response.builder()
                .withStatus(status)
                .withHeaders(invocation.annotationData().responseAnnotationData().headers());
        thenRespond(responseBuilder.build());
    }

    /**
     * Allow a complete custom response to be specified.
     * This is useful when the declarative means to specify a response are not adequate
     * or for testing edge cases where an unexpected response should be produced.
     * <br/>
     * This method is not restricted to generic type T to allow clients to return
     * body objects other than the one defined in the contract.
     *
     * @param response the response.
     * @deprecated use willRespond
     */
    @Deprecated(since = "0.0.4")
    public void willReturnResponse(Response<?> response) {
        willRespond(response);
    }

    /**
     * Allow a complete custom response to be specified.
     * This is useful when the declarative means to specify a response are not adequate
     * or for testing edge cases where an unexpected response should be produced.
     * <br/>
     * This method is not restricted to generic type T to allow clients to return
     * body objects other than the one defined in the contract.
     *
     * @param response the response.
     * @deprecated use thenRespond
     */
    @Deprecated(since = "0.0.4")
    public void willRespond(Response<?> response) {
        thenRespond(response);
    }

    /**
     * Allow a complete custom response to be specified.
     * This is useful when the declarative means to specify a response are not adequate
     * or for testing edge cases where an unexpected response should be produced.
     * <br/>
     * This method is not restricted to generic type T to allow clients to return
     * body objects other than the one defined in the contract.
     *
     * @param response the response.
     */
    public void thenRespond(Response<?> response) {
        mocker.register(invocation, response);
    }

    public void ok() {
        thenReturnStatus(200);
    }

    public void created() {
        thenReturnStatus(201);
    }

    public void accepted() {
        thenReturnStatus(202);
    }

    public void noContent() {
        thenReturnStatus(204);
    }

    public void movedPermanently() {
        thenReturnStatus(301);
    }

    public void temporaryRedirect() {
        thenReturnStatus(307);
    }

    public void permanentRedirect() {
        thenReturnStatus(308);
    }

    public void badRequest() {
        thenReturnStatus(400);
    }

    public void unauthorized() {
        thenReturnStatus(401);
    }

    public void forbidden() {
        thenReturnStatus(403);
    }

    public void notFound() {
        thenReturnStatus(404);
    }

    public void conflict() {
        thenReturnStatus(409);
    }

    public void preConditionFailed() {
        thenReturnStatus(412);
    }

    public void unprocessableEntity() {
        thenReturnStatus(422);
    }

    public void tooManyRequests() {
        thenReturnStatus(429);
    }

    public void serverError() {
        thenReturnStatus(500);
    }

    public void badGateway() {
        thenReturnStatus(502);
    }

    public void serviceUnavailable() {
        thenReturnStatus(503);
    }
}
