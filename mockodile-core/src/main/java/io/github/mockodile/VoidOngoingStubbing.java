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
     */
    public void willReturn() {
        delegate.willReturn(null);
    }

    /**
     * Return the default response.
     * The status param will overwrite any annotated @Response.status().
     * The headers provided will be combined with any headers specified on the @MockedApi.
     * The body will be empty.
     */
    public void willReturn(int status, HttpHeaders headers) {
        delegate.willReturn(status, null, headers);
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
    public void willReturnResponse(Response<?> response) {
        delegate.willReturnResponse(response);
    }
}
