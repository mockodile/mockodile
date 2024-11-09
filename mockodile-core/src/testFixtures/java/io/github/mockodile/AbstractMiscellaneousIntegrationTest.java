package io.github.mockodile;

import io.github.mockodile.annotations.Get;
import io.github.mockodile.annotations.MockedApi;
import io.github.mockodile.annotations.PathParam;
import io.github.mockodile.annotations.RequestMapping;
import org.junit.jupiter.api.Test;

import static io.github.mockodile.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class AbstractMiscellaneousIntegrationTest extends AbstractIntegrationTest {

    private TestMockedApi testMockedApi;

    @Override
    void initMocks(MockApi mockApi) {
        testMockedApi = mockApi.mock(TestMockedApi.class);
    }

    @MockedApi
    @RequestMapping(path = "/root-path")
    interface TestMockedApi {
        @Get(path = "/say-hello")
        String sayHello();

        @Get(path = "/get/id1/{id1}/id2/{id2}")
        String get(@PathParam String id1, @PathParam String id2);
    }

    @Test
    void incompleteCallToTestComplete_callToNextMockAfterIncompleteRequest_illegalArgumentException() {
        // this call is Illegal as it is not completed
        testMockedApi.sayHello();

        // next call to any method will fail
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> testMockedApi.sayHello())
                .withMessageContaining("Incorrect usage of @MockedApi,")
                .withMessageContaining("public abstract java.lang.String io.github.mockodile.AbstractMiscellaneousIntegrationTest$TestMockedApi.sayHello()")
                .satisfies(illegalArgumentException -> assertThat(illegalArgumentException.getCause())
                        .isInstanceOf(Exception.class)
                        .hasMessageContaining("IncorrectUsage: public abstract java.lang.String io.github.mockodile.AbstractMiscellaneousIntegrationTest$TestMockedApi.sayHello()"));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void none_mockWithMixOfArgumentMatchersAndArgs_illegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> testMockedApi.get("id1", eq("id2")));
    }

    @Test
    void methodWithMissingParamAnnotations_createMock_illegalArgumentException() {
        @MockedApi
        @RequestMapping(path = "/root-path")
        interface InvalidClient {
            @Get(path = "/methodWithMissingParamAnnotations/id1/{id1}")
            @SuppressWarnings("unused")
            void methodWithMissingParamAnnotations(@PathParam String annotatedParam, String notAnnotatedParam);
        }

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> mockApi.mock(InvalidClient.class));
    }

    @Test
    void anyClass_createMock_illegalArgumentException() {
        class AnyClass {
        }
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> mockApi.mock(AnyClass.class))
                .withMessageContaining("only works with interfaces");
    }

    @Test
    void anyInterfaceWithoutAnnotation_createMock_illegalArgumentException() {
        interface MissingAnnotation {
        }
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> mockApi.mock(MissingAnnotation.class))
                .withMessageContaining("clients must be annotated with @" + MockedApi.class.getSimpleName());
    }

    @Test
    void annotatedInterfaceWithoutAnyAnnotatedMethods_createMock_illegalArgumentException() {
        @MockedApi
        interface MissingAnnotationOnMethod {
            @SuppressWarnings("unused")
            String get();
        }
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> mockApi.mock(MissingAnnotationOnMethod.class))
                .withMessageContaining("does not have any annotated request methods");
    }

    @Test
    void annotatedInterface_createMock_mockCreated() {
        @MockedApi
        interface Valid {
            @Get
            @SuppressWarnings("unused")
            String get();
        }
        Valid mock = mockApi.mock(Valid.class);
        assertThat(mock).isNotNull();
        assertThat(mock.toString()).contains("MockedApi proxy for interface [%s]".formatted(Valid.class.getName()));
    }
}
