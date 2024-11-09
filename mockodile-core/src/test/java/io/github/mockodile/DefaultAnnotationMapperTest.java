package io.github.mockodile;

import io.github.mockodile.annotations.*;
import io.github.mockodile.domain.AnnotationData;
import io.github.mockodile.domain.ParamType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAnnotationMapperTest {

    private final DefaultAnnotationMapper defaultAnnotationMapper = new DefaultAnnotationMapper();

    @RequestMapping(path = "/root")
    private interface ConcreteWithRootPathWithLeadingSlash {
        @Get(path = "/concrete")
        @SuppressWarnings("unused")
        String getConcrete();
    }

    @RequestMapping(path = "root")
    private interface ConcreteWithRootPathWithoutLeadingSlash {
        @Get(path = "/concrete")
        @SuppressWarnings("unused")
        String getConcrete();
    }

    @RequestMapping(path = "/root")
    interface ConcreteWithMethodPathWithoutLeadingSlash {
        @Get(path = "concrete")
        @SuppressWarnings("unused")
        String getConcrete();
    }

    @ParameterizedTest
    @ValueSource(classes = { ConcreteWithRootPathWithLeadingSlash.class, ConcreteWithRootPathWithoutLeadingSlash.class, ConcreteWithMethodPathWithoutLeadingSlash.class } )
    void pathAnnotationOnTypeAndMethod_map_mapped(Class<?> concreteClass) throws Exception {
        var result = defaultAnnotationMapper.from(concreteClass, concreteClass.getMethod("getConcrete"));
        assertThat(result).isEqualTo(
                AnnotationData.builder()
                        .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder.withPath("/root/concrete"))
                        .build());
    }

    @Test
    void pathAnnotationOnTypeOnly_map_mapped() throws Exception {
        @RequestMapping(path = "/root")
        interface UnderTest {
            @Get
            String getConcrete();
        }

        var result = defaultAnnotationMapper.from(UnderTest.class, UnderTest.class.getMethod("getConcrete"));
        assertThat(result).isEqualTo(AnnotationData.builder()
                .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder.withPath("/root"))
                .build());
    }

    @Test
    void pathAnnotationOnMethodOnly_map_mapped() throws Exception {
        @RequestMapping
        interface UnderTest {
            @Get(path = "/concrete")
            String getConcrete();
        }

        var result = defaultAnnotationMapper.from(UnderTest.class, UnderTest.class.getMethod("getConcrete"));
        assertThat(result).isEqualTo(AnnotationData.builder()
                .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder.withPath("/concrete"))
                .build());
    }

    @Test
    void noPathAnnotationSpecified_map_mapped() throws Exception {
        @RequestMapping
        interface UnderTest {
            @Get
            String getConcrete();
        }

        var result = defaultAnnotationMapper.from(UnderTest.class, UnderTest.class.getMethod("getConcrete"));
        assertThat(result).isEqualTo(AnnotationData.builder()
                .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder.withPath("/"))
                .build());
    }

    @Test
    void headerAnnotationOnTypeAndMethod_map_mapped() throws Exception {
        @RequestMapping(headers = {"h1=v1", "h2=v2"})
        interface UnderTest {
            @Get(headers = {"h2=v2b", "h3=v3"})
            String getConcrete();
        }

        var result = defaultAnnotationMapper.from(UnderTest.class, UnderTest.class.getMethod("getConcrete"));
        assertThat(result).isEqualTo(AnnotationData.builder()
                .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder
                        .withPath("/")
                        .withHeader("h1", "v1")
                        .withHeader("h2", "v2", "v2b")
                        .withHeader("h3", "v3"))
                .build());
    }

    @Test
    void headerAnnotationOnTypeOnly_map_mapped() throws Exception {
        @RequestMapping(headers = {"h1=v1", "h2=v2"})
        interface UnderTest {
            @Get
            String getConcrete();
        }

        var result = defaultAnnotationMapper.from(UnderTest.class, UnderTest.class.getMethod("getConcrete"));
        assertThat(result).isEqualTo(AnnotationData.builder()
                .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder
                        .withPath("/")
                        .withHeader("h1", "v1")
                        .withHeader("h2", "v2"))
                .build());
    }

    @Test
    void headerAnnotationOnMethodOnly_map_mapped() throws Exception {
        @RequestMapping
        interface UnderTest {
            @Get(headers = {"h1=v1", "h2=v2"})
            String getConcrete();
        }

        var result = defaultAnnotationMapper.from(UnderTest.class, UnderTest.class.getMethod("getConcrete"));
        assertThat(result).isEqualTo(AnnotationData.builder()
                .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder
                        .withPath("/")
                        .withHeader("h1", "v1")
                        .withHeader("h2", "v2"))
                .build());
    }

    @Test
    void paramMappingWithNames_map_mapped() throws Exception {
        @RequestMapping
        interface UnderTest {
            @Get(path = "/param/{id}")
            String getWithParams(@HeaderParam("h1") String header1, @PathParam("id") String identifier, @QueryParam("q1") String queryParam);
        }

        var method = UnderTest.class.getMethod("getWithParams", String.class, String.class, String.class);
        var result = defaultAnnotationMapper.from(UnderTest.class, method);
        assertThat(result).isEqualTo(AnnotationData.builder()
                .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder
                        .withPath("/param/{id}")
                        .withParam(ParamType.HEADER, "h1")
                        .withParam(ParamType.PATH, "id")
                        .withParam(ParamType.QUERY, "q1"))
                .build());
    }

    @Test
    void paramMappingWithNoNames_map_mapped() throws Exception {
        @RequestMapping
        interface UnderTest {
            @Get(path = "/param/{id}")
            String getWithParams(@HeaderParam String header1, @PathParam String identifier, @QueryParam String queryParam);
        }

        var method = UnderTest.class.getMethod("getWithParams", String.class, String.class, String.class);
        var result = defaultAnnotationMapper.from(UnderTest.class, method);
        assertThat(result).isEqualTo(AnnotationData.builder()
                .request(requestAnnotationDataBuilder -> requestAnnotationDataBuilder
                        .withPath("/param/{id}")
                        .withParam(ParamType.HEADER, "header1")
                        .withParam(ParamType.PATH, "identifier")
                        .withParam(ParamType.QUERY, "queryParam"))
                .build());
    }
}