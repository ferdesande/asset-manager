# Asset Manager
Upload coordination and searchable asset catalog

## Architecture

This project uses:
- **Hexagonal architecture** Ports and adapters
- **Virtual Threads** Leveraging Java 21 for async processing
- **Spring Boot 3** with Spring MVC (not WebFlux)

Key architectural decision and their rationale are documented in [docs/DECISION.md](docs/DECISION.md).