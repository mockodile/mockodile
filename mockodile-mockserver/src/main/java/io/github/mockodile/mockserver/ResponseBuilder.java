package io.github.mockodile.mockserver;

import io.github.mockodile.ObjectMapper;
import io.github.mockodile.domain.Body;
import io.github.mockodile.domain.Response;
import org.mockserver.model.HttpResponse;

import static org.mockserver.model.HttpResponse.response;

class ResponseBuilder<T> {
    private final ObjectMapper objectMapper;
    private final Response<T> response;

    ResponseBuilder(ObjectMapper objectMapper, Response<T> response) {
        this.objectMapper = objectMapper;
        this.response = response;
    }

    HttpResponse build() {
        var responseBuilder = response().withStatusCode(response.status());

        response.headers().toMap().forEach((key, value) -> responseBuilder.withHeader(key, value.toArray(new String[0])));
        if (response.body() != null) {
            Body<?> body = response.body();
            if (body.isRaw()) {
                responseBuilder.withBody(body.getRaw());
            } else if (body.isString()) {
                responseBuilder.withBody(body.getString());
            } else {
                responseBuilder.withBody(objectMapper.writeValueAsString(body.getObject()));
            }
        }
        return responseBuilder;
    }

}
