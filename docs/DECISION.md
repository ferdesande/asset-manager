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