# Architectural Decisions

This document explains the key design choices made for the Asset Manager Implementation

## 1. Hexagonal Architecture
**Decision**: 

Implement the Asset Manager using Hexagonal Architecture (Ports & Adapters pattern).

**Rationale**:

The assignment explicitly mentions hexagonal design as an option. This architecture provides:
1. **Clear separation of concerns**: Domain logic isolated from infrastructure (persistence, HTTP, external clients)
2. **Testability**: Domain can be tested without Spring context or database
3. **Flexibility**: Easy to swap implementations (e.g., mock publisher → real HTTP client)
4. **Alignment with best practices**: Industry-standard pattern for maintainable systems
   Given the requirement and these benefits, hexagonal architecture is the most appropriate choice.
   
**Trade-offs**:

- **More boilerplate**: Requires mappers between domain entities and infrastructure DTOs
- **Initial complexity**: More packages/interfaces than a simple layered architecture
- **Acceptable for scope**: The structure benefits outweigh the boilerplate in a system designed to demonstrate
architecture skills

## 2. Virtual Threads over WebFlux
**Decision**: 

Use Java 21 Virtual Threads with Spring MVC instead of Spring WebFlux for async processing.

**Context**:

The system requires asynchronous upload processing with better performance than traditional thread pools. 
Both WebFlux and Virtual Threads meet this requirement - the decision is about which approach fits better 
for this specific use case.

**Rationale**:

Both technologies provide the necessary async capabilities:
- **WebFlux**: Achieves concurrency through reactive non-blocking I/O
- **Virtual Threads**: Achieves concurrency through lightweight JVM threads

I chose Virtual Threads because:

1. **Simpler programming model**: Imperative code is more straightforward for this use case
2. **Compatibility**: Works with blocking libraries (JDBC, RestTemplate) without requiring reactive alternatives
3. **Sufficient for scope**: Virtual Threads handle millions of concurrent operations, far exceeding proxy system needs
4. **Faster development**: No learning curve for reactive operators, simpler testing

I have experience with WebFlux, so this was an informed choice rather than avoiding complexity.

**When WebFlux Would Be Preferred**:

WebFlux would be the better choice if:
- The system requires **backpressure management** (e.g., rate-limiting incoming streams)
- We need **reactive end-to-end** integration (Kafka Reactive → WebFlux → R2DBC)
- The system expects a **high number of requests per second**
- The infrastructure already has **reactive components** to integrate with
- There were **sustained millions of requests/second** requiring non-blocking I/O at scale

For a proxy system with simple upload → save metadata flow, these scenarios don't apply.

**Alternatives Considered**:

- **WebFlux + Project Reactor**:
    Pros: Fully reactive, native backpressure
    Cons: Requires R2DBC, more complex testing
    Conclusion: Valid solution, but adds unnecessary complexity for this scope

- **Traditional thread pools**:
    Pros: Simple, well-known
    Cons: Limited scalability, high memory per thread
    Conclusion: Insufficient performance compared to requirements

**Trade-offs**:

- No reactive operators (not needed for this use case)
- Not fully reactive (acceptable for proxy pattern)
- Requires Java 21+ (already a requirement)

**Outcome**: Virtual Threads deliver the required async performance with maintainable imperative code. 
The hexagonal architecture allows migrating to WebFlux if reactive requirements emerge.

## 3. Async Method Invocation with Self-Reference

**Decision**

Use ``ObjectProvider<AssetService>`` for self referencing when calling ``@Async`` methods within the same class.

**Context**

Spring's ``@Async`` annotation uses AOP proxies to execute methods asynchronously. A common pitfall is 
**self-invocation** when a method calls another ``@Async`` method in the same class, the proxy is bypassed and the
call is executed synchronously.

```java
import org.springframework.scheduling.annotation.Async;

// ❌ This does not work
public class FooService {
    public void doSomething() {
        // previous code
        doSomethingAsync();
    }

    @Async
    public void doSomethingAsync(){
        // Executes in the SAME thread (Synchronously)
    }
}
```

**Solution**

We decided to use self-injection with ``ObjectProvider<>``. It must be done in the following way if a 
config class is used to create beans.

```java
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class FooConfig {
    @Bean
    public FooService fooService(
            // other dependencies 
            @Lazy ObjectProvider<FooService> selfProvider
            ) {
        return new FooService(/*other dependencies ,*/ selfProvider);
    }
}

public class FooService {
    private final ObjectProvider<FooService> selfProvider;
    
    public void doSomething() {
        // previous code
        selfProvider.getObject().doSomethingAsync();
    }

    @Async
    public void doSomethingAsync(){
        // Now is executed in a Virtual Thread.
    }
}
```

**Rationale**
1. **Compatible with manual bean creation**: Works with ``@Bean`` and ``@Configuration``
2. **Explicit**: The self-reference is visible in the constructor signature
3. **Lazy loading**: Avoids circular dependencies
4. **Standard Spring Pattern**: ``ObjectProvider<>`` is the recommended Spring approach for optional/lazy dependencies.

**Trade-offs**
- **Verbosity**: ``selfProvider.getObject().doSomethingAsync();`` is worse than just ``this.doSomethingAsync();``

**Alternatives considered**

In the case of the ``AssetService`` other alternatives were considered.

1. **Self-injection with ``@Lazy``**: Inject the bean directly in the service with ``@Lazy``.
- Pros: Simple annotation
- Cons: Does not work when you want to isolate application and domain layers from the framework
2. **Separated class**: Extract the async logic into another class
- Pros: Does not need self-injection
- Cons: Additional class, spread Asset storage through different classes.

**Test implications**

In **Unit testing** (tests annotated with ``@ExtendWith(MockitoExtension.class``) the ``ObjectProvider<>`` 
must be mocked everywhere and the annotation ``@InjectMocks`` cannot be used.

In **Integration testing** (tests annotated with ``@SpringBootTest``) the Spring context handles the self-reference
automatically.
