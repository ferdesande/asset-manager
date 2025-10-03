# Asset Manager
Upload coordination and searchable asset catalog

## Architecture

This project uses:
- **Hexagonal architecture** Ports and adapters
- **Virtual Threads** Leveraging Java 21 for async processing
- **Spring Boot 3** with Spring MVC (not WebFlux)

Key architectural decision and their rationale are documented in [docs/DECISION.md](docs/DECISION.md).

### Architecture Validation

The hexagonal architecture is enforced through **automated tests** using [ArchUnit](https://www.archunit.org/):

- **PlantUML diagram validation**: Ensures the codebase adheres to the architecture diagram defined in
[architecture.puml](src/test/resources/architecture.puml)
- **Layer dependency rules**: Validates that Domain remains free of framework dependencies and Infrastructure correctly
implements ports
- **Port contracts**: Ensures all ports are interfaces following hexagonal principles
- **Naming conventions**: Enforces consistent naming across layers (entities, repositories, controllers)
- **Cycle detection**: Prevents circular dependencies between packages

These tests run automatically in the CI/CD pipeline and fail the build if architectural violations are detected.

Run architecture tests:
```bash
./mvnw test -Dtest=ArchitectureTest
```

## Project Structure
```
src/main/java/com/inditex/assetmanager/
├── domain/                     # Core business logic
│   ├── model/                  # Entities
│   ├── port/
│   │   ├── in/                 # Use cases
│   │   └── out/                # Output ports
│   └── service/                # Domain services implementing use cases
│
├── application/                # Orchestration layer
│   └── AssetService            # Coordinates domain + ports
│
└── infrastructure/             # Adapters (Spring, DB, HTTP)
    ├── adapter/
    │   ├── in/
    │   │   └── rest/           # REST Controllers
    │   └── out/
    │       ├── persistence/    # JPA repositories & entities
    │       └── publisher/      # Client to external publisher
    └── config/                 # Spring configuration
```
