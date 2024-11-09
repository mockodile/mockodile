package io.github.mockodile.sb.it;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.RequestMapping;

@MockedApi
@RequestMapping(path = "/root-path")
interface HelloWorldApi {
    @Get(path = "/say-hello")
    String sayHello();
}
