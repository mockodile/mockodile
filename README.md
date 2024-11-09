# Mockodile

Mockodile provides a new approach to mocking rest apis in tests, to really give your integration tests some bite.

## Introduction

The developers of *Mockodile* believe that tests which test the boundaries of your system are an extremely valuable resource.
Other than seeing it work in production, such tests offer the most confident view of how a system actually functions.

Frameworks like spring boot make it easy for us to start our application and run tests against a running application.
Mocking frameworks like wiremock or mock-server let us mock away rest calls to external systems.

Despite the existence of such excellent frameworks/libraries it is still hard to write tests which mock system boundaries 
and typically tests become polluted with details and quickly become hard to understand and maintain - or even worse 
developers choose not to write integration tests at all.

*Mockodile* aims to make it easy and intuitive to use mocking frameworks such as wiremock or mock-server as well as to read and maintain tests.
*Mockodile* currently leans heavily on the work of the wiremock and mock-server to achieve this, but it offers a new declarative way to define mocks.

Setting up mock expectations should be as easy and readable as with other mocking frameworks such as mockito:

    mockApi.when(externalOrderSystem.getOrderById(1)).thenReturn(testOrder);

Verifying that a request was made correctly should also be as easy and readable as with other mocking frameworks like mockito:

    var createdOrder = mockApi.verify(externalOrderSystem.createOrder()).andDeserializeRequestBody(Order.class);
    assertThat(createdOrder.items()).hasSize(1);
    // ...

*Mockodile* does **not** aim to cover all special edge cases of external systems. It aims to make it easy to test the majority 
of common system interactions, such as sending and receiving Json objects over http. When Mockodile does not meet the needs of 
special cases within your tests it is still possible to use wiremock of mock-server directly in parallel to Mockodile.

# Getting started

When getting started, you should choose which mocking implementation you should use behind mockodile.
Mockodile currently supports wiremock and mock-server.

# Getting started with wiremock

Add a dependency to the project mockodile-wiremock

**TODO** define how to depend on this project once it has been published.

If you are using spring boot consider looking instead at the module mockodile-wiremock-spring-boot module

If you are not using spring boot mockodile and wiremock can be started with a junit extension, here is a basic starting point.

    @ExtendWith(MockodileWiremockExtension.class)
    class MyTest {
        protected MockApi mockApi;
        protected String mockUrl;
        
        @BeforeEach
        protected void setUp(MockApiProvider mockApiProvider) {
            this.mockApi = mockApiProvider.mockApi();
            mockApi.reset();
            mockUrl = "http://localhost:" + mockApiProvider.port();
            // TODO init mocks
        }
    }

# Getting started with mock-server

Add a dependency to the project mockodile-mockserver

**TODO** define how to depend on this project once it has been published.

If you are using spring boot consider looking instead at the module mockodile-mockserver-spring-boot module

Once you have Mockodile initialized in your test you are ready to create mock objects, define stubs and responses, and verify calls.

If you are not using spring boot mockodile and mock-server can be started with a junit extension, here is a basic starting point.


    @ExtendWith(MockodileMockServerExtension.class)
    class MyTest {
        protected MockApi mockApi;
        protected String mockUrl;
        
        @BeforeEach
        protected void setUp(MockApiProvider mockApiProvider) {
            this.mockApi = mockApiProvider.mockApi();
            mockApi.reset();
            mockUrl = "http://localhost:" + mockApiProvider.port();
            // TODO init mocks
        }
    }

## Defining a Mock

If you are familiar with frameworks like Feign or perhaps spring-data defining a mock will feel very familiar to you.
Even if you are not familiar with such frameworks the declarative approach is still very easy understand.

For a given system or sub system you wish to mock away, define an interface which represents the contract of that system.

Imagine we integrate with an external system which allows us to create read & update a Todo List we could define the contract of our 
mock as follows:

    @MockedApi
    @RequestMapping("/todo")
    class TodoMock {

        @Post(path = "/create")
        @Response(status = 201)
        void create(@RequestBody TodoItemDto todoItem);

        @Get(path = /by-id/{id}")
        TodoItemDto getById(@PathParam String id);
        
        @Put(path = /by-id/{id}")
        void done(@PathParam String id);
    }

This just defines the static contract of our mock, paths and url parameters etc., not the actual behaviour which is mocked.
for a specific test.

## Creating the mock object in test class

Now that we have a mock contract defined we want to specify behaviour in each of our tests.
In some cases this will be returning success responses, in others it might be as simple as specifying a not found error response.

We have already initialized Mockodile in our test, now we need to initialize the mock objects based on the contract we defined.
This can be done in the test method itself or in the initialization of the test:

    this.todoMock = mockApi.mock(TodoMock.class)

## Defining behaviour

Now within a specific test we can define specific behaviour

    @Test
    void givenAny_whenCreateNewAccountRequest_newTodoItemCreated() {
        // arrange
        mockApi.when(() -> todoMock.create(ArgumentMatchers.anyBody()).willReturnEmptyBody();
        
        // ... 
        // TODO call the method/service/endpoint under test

        // TODO see below verify correctness of call to create endpoint
    }

    @Test
    void todoItemAlreadyExists_getTodoItem_todoItemReturned() {
        // arrange
        String id = "1234";
        mockApi.when(todoMock.getById(id)).willReturn(new TodoItemDto("1234", "go to the shops");

        // ... 
        // TODO call the method/service/endpoint under test
}

The first test arrange line above tells wiremock that when it receives a POST request **/todo/create** with any kind of request body, 
then it should return 201.

_Note that for void methods a lambda is used when specifying behaviour_

The second test arrange line above tells wiremock that which it receives a GET request for the path **/todo/by-id/1234** 
that it should return the Json object represented by the given TodoItemDto, for example this could be:

    {"id":1234,"item":"go to the shops"}

As you can see, the "arrange" section of the test is readable, it is free from complications such as http methods and status codes,
and free from headers, paths etc.
Instead, the code is readable and can be designed to map closely to the domain of your application.

## Verifying behaviour

The "act" part of the test is out of scope of Mockodile, after which Mockodile can be used in the "assert" part of a test
to verify requests received etc.

To check that a request was actually received use the verify method as follows

    mockApi.verify(() -> todoMock.create(ArgumentMatchers.anyBody()));

Or as follows:

    mockApi.verify(todoMock.getById(id));

In addition, it is possible to check for number of requests as well as to get the details of the actual requests which were sent.

For example to get the actually received request body deserialized to a pojo:

    TodoItemDto actualRequestBody = mockApi.verify(() -> todoMock.create(ArgumentMatchers.anyBody()))
        .andDeserializeRequestBody(TodoItemDto.class);
    // verify contents of request body

## Best practices

Like wiremock, Mockodile tries to be flexible, it is up to developers to decide how to use it.

However, as a starting point here are some best practices for using Mockodile.

### Best practice : match on the minimum verify the maximum

In the "arrange" part of your test it is tempting to match on all parts of the request so that the mocked response
will only return the defined response in case the request matches 100%. This however makes tests very difficult to read 
and understand and even harder to understand when something goes wrong. This is because the wiremock server will just log that 
the request was not matched and even though wiremock tries to provide a hint about why the request wasn't matched, this hint
is just somewhere in the console log. In addition the test does not fail because of an unmatched request, instead it fails
because of a side effect of the unmatched request such as wiremock returning 404 instead of the expected valid response.

So best practice is to think about how your requests to external systems can be uniquely identified. 
In many cases the path with an ID is enough to uniquely identify the request.
In other cases such as a POST to create a resource, a single field in the body such as an identifier is enough to uniquely 
identify the request.

Effectively what we want to be able to arrange in our test is that when request A is received return response A, so the
easiest way to identify request A should be used.

This does not mean however that all the other details of the request are un-important. On the contrary it is vital
that all relevant details of the request should be validated. This however should be done in the "assert" part of the test.
Mockodile (and wiremock) supports this be exposing the actual request which was sent. An assert framework can be used
to verify all the details of the request including body, headers, etc.

For example a post for a resource to create a new order item might look like this

    POST /order/1234/item

    {
        "iban" : "9876-5432-1234-5678",
        "title" : "How to make friends and influence people",
        "quantity" : 2,
        "allowToBeShippedSeparatly": true
        ...
        ...
    }

For testing purposes it is probably enough to define the mock for this to match only on the path and on the iban in the body

This can be done as follows:

    @MockedApi
    @RequestMapping(path = "/order")
    interface OrderItemMockedApi {
        @Post(path = "/{orderId}/item")
        void createItem(@PathParam String orderId, @JsonPath(expression = "$.iban") String iban);
    }

This can then be used in our test as follows

    // arrange
    mockApi.when(() -> orderItemMockedApi.createItem("1234", "9876-5432-1234-5678")).willReturn();

    // act
    // ... not shown
    
    // assert
    var actaulOrderItem = mockApi.verify(() -> orderItemMockedApi.createItem("1234", "9876-5432-1234-5678")).andDeserializeRequestBody(OrderItem.class);
    
    // example using assertj to assert all fields of the request body
    assertThat(actaulOrderItem).isEqualTo(
        OrderItem.builder()
            .iban("9876-5432-1234-5678")
            .title("How to make friends and influence people")
            .quantity(2)
            .allowToBeShippedSeparatly(true)
            .build());
    
As is demonstrated here, Mockodile is used to help arrange the test but the majority of the "assert" logic is done
using other assertion frameworks (of your choice).