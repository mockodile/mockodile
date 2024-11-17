package io.github.mockodile.domain;

import io.github.mockodile.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RequestAnnotationDataBuilder {

    public static final String PATH_DELIMITER = "/";
    private HttpMethod httpMethod;
    private String path = "";
    private final HttpHeaders.HttpHeadersBuilder httpHeadersBuilder = HttpHeaders.builder();
    private final List<ParamAnnotationData> paramAnnotationData = new ArrayList<>();
    private List<String> jsonPaths = new ArrayList<>();

    public RequestAnnotationDataBuilder withPath(String path) {
        if (!path.startsWith(PATH_DELIMITER)) {
            this.path = this.path + PATH_DELIMITER;
        }
        this.path = this.path + path;
        return this;
    }

    public RequestAnnotationDataBuilder withHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public RequestAnnotationDataBuilder withHeader(String headerName, String... headerValue) {
        httpHeadersBuilder.put(headerName, List.of(headerValue));
        return this;
    }

    public RequestAnnotationDataBuilder withHeader(String headerName, List<String> headerValues) {
        httpHeadersBuilder.put(headerName, headerValues);
        return this;
    }

    public RequestAnnotationDataBuilder withHeaders(List<String> headers) {
        httpHeadersBuilder.addAll(headers);
        return this;
    }

    public RequestAnnotationDataBuilder withParam(ParamType paramType, String name) {
        return withParam(new ParamAnnotationData(paramType, name));
    }

    private RequestAnnotationDataBuilder withParam(ParamAnnotationData entry) {
        paramAnnotationData.add(entry);
        return this;
    }

    public void withJsonPath(String[] jsonPaths) {
        this.jsonPaths = Arrays.asList(jsonPaths);
    }

    public RequestAnnotationData build() {
        return new RequestAnnotationData(
                httpMethod != null ? httpMethod : HttpMethod.GET,
                StringUtils.hasText(path) ? path : PATH_DELIMITER,
                httpHeadersBuilder.build(),
                Collections.unmodifiableList(paramAnnotationData),
                Collections.unmodifiableList(jsonPaths));
    }
}
