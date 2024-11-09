package io.github.mockodile.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.mockodile.ObjectMapper;
import io.github.mockodile.domain.Body;
import io.github.mockodile.domain.Response;

public class ResponseBuilder<T> {

    private final ObjectMapper objectMapper;
    private final Response<T> response;

    public ResponseBuilder(ObjectMapper objectMapper, Response<T> response) {
        this.objectMapper = objectMapper;
        this.response = response;
    }

    ResponseDefinitionBuilder build() {
        var responseBuilder = WireMock.aResponse()
                .withStatus(response.status());

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
