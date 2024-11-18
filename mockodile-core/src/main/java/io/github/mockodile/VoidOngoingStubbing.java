package io.github.mockodile;

import io.github.mockodile.annotations.HeaderParam;
import io.github.mockodile.domain.HttpHeaders;
import io.github.mockodile.domain.Response;

/**
 * Allows for specification of responses for void mocked apis.
 * Returned by {@link MockApi#when(Runnable)} to allow clients to specify responses using a fluid api.
 * <br/>
 * Example use:
 * <code>mockApi.when(() -> myApi.anyMethod()).willReturn(...)</code>
 */
public class VoidOngoingStubbing {

    private final OngoingStubbing<Void> delegate;

    public VoidOngoingStubbing(OngoingStubbing<Void> delegate) {
        this.delegate = delegate;
    }

    /**
     * Return the default response.
     * The status will be the status specified on the method of the @MockedApi method with the annotation.
     * {@link Response#status()} or 200 by default.
     * The headers will include any headers specified on the @MockedApi method with
     * {@link HeaderParam}.
     * The body will be empty.
     *
     * @deprecated use thenReturnDefault()
     */
    @Deprecated(since = "0.0.4")
    public void willReturn() {
        thenReturnDefault();
    }

    /**
     * Return the default response.
     * The status will be the status specified on the method of the @MockedApi method with the annotation.
     * {@link Response#status()} or 200 by default.
     * The headers will include any headers specified on the @MockedApi method with
     * {@link HeaderParam}.
     * The body will be empty.
     */
    public void thenReturnDefault() {
        delegate.thenReturn(null);
    }

    /**
     * Return the default response.
     * The status param will overwrite any annotated @Response.status().
     * The headers provided will be empty.
     * The body will be empty.
     */
    public void thenReturnStatus(int status) {
        thenReturnStatusAndHeader(status, HttpHeaders.empty());
    }

    /**
     * Return the default response.
     * The status param will overwrite any annotated @Response.status().
     * The headers provided will be combined with any headers specified on the @MockedApi.
     * The body will be empty.
     * @deprecated use thenReturnStatusAndHeader()
     */
    @Deprecated(since = "0.0.4")
    public void willReturn(int status, HttpHeaders headers) {
        thenReturnStatusAndHeader(status, headers);
    }

    /**
     * Return the default response.
     * The status param will overwrite any annotated @Response.status().
     * The headers provided will be combined with any headers specified on the @MockedApi.
     * The body will be empty.
     */
    public void thenReturnStatusAndHeader(int status, HttpHeaders headers) {
        delegate.thenRespond(status, null, headers);
    }

    /**
     * Allow a complete custom response to be specified.
     * This is useful when the declarative means to specify a response are not adequate
     * or for testing edge cases where an unexpected response should be produced.
     * <br/>
     * This method allows clients to return any type of body even for void methods.
     *
     * @param response the response.
     *
     * @deprecated use thenRespond()
     */
    @Deprecated(since = "0.0.4")
    public void willReturnResponse(Response<?> response) {
        thenRespond(response);
    }

    /**
     * Allow a complete custom response to be specified.
     * This is useful when the declarative means to specify a response are not adequate
     * or for testing edge cases where an unexpected response should be produced.
     * <br/>
     * This method allows clients to return any type of body even for void methods.
     *
     * @param response the response.
     */
    public void thenRespond(Response<?> response) {
        delegate.thenRespond(response);
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
