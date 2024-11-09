package io.github.mockodile.sb;

import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.mockserver.springtest.MockServerTestExecutionListener;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.event.*;
import org.springframework.test.context.event.annotation.*;

import java.lang.reflect.Method;

/**
 * This class wraps the MockServerTestExecutionListener which uses static variables into spring events.
 * The MockServerClient which is created by the MockServerTestExecutionListener is exposed to Mockodile
 * using the MockServerClientHolder.
 *
 * When mockodile needs the MockServerClient it fetches it from the MockServerClientHolder on demand and
 * therefore receives the instance which has been instantiated for the specific test method.
 */
public class MyTestExecutionListener {

    private final MockServerTestExecutionListener mockServerTestExecutionListener = new MockServerTestExecutionListener();
    private final MockServerClientHolder mockServerProvider;

    public MyTestExecutionListener(MockServerClientHolder mockServerProvider) {
        this.mockServerProvider = mockServerProvider;
    }

    @BeforeTestClass
    public void beforeTestClass(BeforeTestClassEvent event) throws Exception {
        mockServerTestExecutionListener.beforeTestClass(event.getTestContext());
    }

    @PrepareTestInstance
    public void prepareTestInstance(PrepareTestInstanceEvent event) throws Exception {
        DummyTestContext dummyTestContext = new DummyTestContext(event.getTestContext().getApplicationContext());

        // Delegate to the MockServerTestExecutionListener which initializes static fields.
        // This is in order for mockodile to be compatible with @MockServerTest
        // and to avoid instantiating mock server client twice
        mockServerTestExecutionListener.prepareTestInstance(dummyTestContext);
        MockServerClient mockServerClient = dummyTestContext.getMockServerClient();
        mockServerProvider.setMockServerClient(mockServerClient);
    }

    @BeforeTestMethod
    public void beforeTestMethod(BeforeTestMethodEvent event) throws Exception {
        mockServerTestExecutionListener.beforeTestMethod(new DummyTestContext(event.getTestContext().getApplicationContext()));
    }

    @BeforeTestExecution
    public void beforeTestExecution(BeforeTestExecutionEvent event) throws Exception {
        mockServerTestExecutionListener.beforeTestExecution(new DummyTestContext(event.getTestContext().getApplicationContext()));
    }

    @AfterTestExecution
    public void afterTestExecution(AfterTestExecutionEvent event) throws Exception {
        mockServerTestExecutionListener.afterTestExecution(new DummyTestContext(event.getTestContext().getApplicationContext()));
    }

    @AfterTestMethod
    public void afterTestMethod(AfterTestMethodEvent event) {
        mockServerTestExecutionListener.afterTestMethod(new DummyTestContext(event.getTestContext().getApplicationContext()));
    }

    @AfterTestClass
    public void afterTestClass(AfterTestClassEvent event) throws Exception {
        mockServerTestExecutionListener.afterTestClass(new DummyTestContext(event.getTestContext().getApplicationContext()));
    }

    /**
     * an instance of this class is passed to MockServerTestExecutionListener so that MockServerTestExecutionListener
     * can get the application context and the DummyTestClass instance in order to initialize the MockServerClient.
     */
    private static class DummyTestContext implements TestContext {
        private final transient ApplicationContext applicationContext;
        private final Class<DummyTestClass> testClass;
        private final transient DummyTestClass testInstance;

        public DummyTestContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
            this.testClass = DummyTestClass.class;
            this.testInstance = new DummyTestClass();
        }

        @Override
        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        @Override
        public Class<?> getTestClass() {
            return testClass;
        }

        @Override
        public Object getTestInstance() {
            return testInstance;
        }

        @Override
        public Method getTestMethod() {
            // method not used by MockServerTestExecutionListener
            return null;
        }

        @Override
        public Throwable getTestException() {
            // method not used by MockServerTestExecutionListener
            return null;
        }

        @Override
        public void markApplicationContextDirty(DirtiesContext.HierarchyMode hierarchyMode) {
            // method not used by MockServerTestExecutionListener
        }

        @Override
        public void updateState(Object testInstance, Method testMethod, Throwable testException) {
            // method not used by MockServerTestExecutionListener
        }

        @Override
        public void setAttribute(String name, Object value) {
            // method not used by MockServerTestExecutionListener
        }

        @Override
        public Object getAttribute(String name) {
            // method not used by MockServerTestExecutionListener
            return null;
        }

        @Override
        public Object removeAttribute(String name) {
            // method not used by MockServerTestExecutionListener
            return null;
        }

        @Override
        public boolean hasAttribute(String name) {
            // method not used by MockServerTestExecutionListener
            return false;
        }

        @Override
        public String[] attributeNames() {
            // method not used by MockServerTestExecutionListener
            return new String[0];
        }

        public MockServerClient getMockServerClient() {
            return testInstance.getMockServerClient();
        }
    }

    /**
     * passed to the MockServerTestExecutionListener as the test being executed
     * so that MockServerTestExecutionListener will initialize the field mockServerClient.
     */
    @MockServerTest
    private static class DummyTestClass {
        @SuppressWarnings("UnusedDeclaration")
        private MockServerClient mockServerClient;

        public MockServerClient getMockServerClient() {
            return mockServerClient;
        }
    }
}
