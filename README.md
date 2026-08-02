# ecommerce-platform

A multi-module Java ecommerce example demonstrating an order/payment/inventory flow implemented as small Spring Boot services coordinated by an orchestrator.

Languages & Tech
- Java (project property: java.version = 26)
- Spring Boot (spring.boot.version = 4.1.0)
- Build: Maven (multi-module)
- Kafka for async messaging (producers/consumers present)
- Swagger for API docs

Modules / Project structure
- common — shared DTOs, events, utilities, saga classes
- order-service — order API, service, repository, Kafka producer
- payment-service — payment API, service, Kafka consumer/producer
- inventory-service — inventory API, service, repository, Kafka producer
- orchestrator — coordinates services, contains orchestrator service/controller and repository

Architecture & Design patterns
- Microservices (each module is a bounded service)
- Event-driven / message-based communication using Kafka (events in common/events)
- Saga pattern for distributed transaction/workflow coordination (common/saga)
- Layered architecture: Controller → Service → Repository
- Repository pattern (per-service repositories) and DTO/event patterns for decoupling

Quick build & run
- Build all modules: `./mvnw clean package` (or `mvn clean package`)
- Run a service: `java -jar <module>/target/*.jar` or run via IDE

See copilot-instructions.md for common Copilot CLI commands and developer notes.
