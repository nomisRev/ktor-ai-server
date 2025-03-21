# Ktor Sample Project

A modern Kotlin backend application built with Ktor, showcasing best practices for building scalable and maintainable web services.

## Tech Stack

- **Framework**: Ktor
- **Database**: PostgreSQL with Exposed ORM and Flyway migrations
- **Security**: JWT Authentication, BouncyCastle Argon2 for password hashing
- **Validation**: Jakarta Validation
- **AI Integration**: LangChain4j
- **Testing**: kotlin-test and assert, Testcontainers

## Features

| Feature                   | Description                                                                        |
|---------------------------|------------------------------------------------------------------------------------|
| User Management           | Complete user CRUD operations with secure password handling using Argon2           |
| JWT Authentication        | Secure API access with JSON Web Tokens                                             |
| Content Negotiation       | Automatic content conversion according to Content-Type and Accept headers          |
| Structured Routing        | Clean and organized API endpoints using Ktor's routing DSL                         |
| Database Integration      | PostgreSQL with connection pooling via HikariCP and Flyway migrations              |
| Input Validation          | Request validation using Jakarta Validation                                        |
| AI Capabilities           | Integration with LangChain4j for AI-powered features                               |
| Metrics & Monitoring      | Prometheus metrics collection and Grafana dashboards for performance monitoring    |
| Serialization             | JSON serialization using kotlinx.serialization                                     |
| Docker Support            | Containerization for easy deployment and testing                                   |

## Setup and Configuration

### Prerequisites

- JDK 11 or higher
- Docker (for running PostgreSQL and tests with Testcontainers)
- Gradle

### Running the Application

To build or run the project, use one of the following Gradle tasks:

| Task                          | Description                                                          |
|-------------------------------|---------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `./gradlew buildFatJar`       | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`        | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                           |
| `./gradlew run`               | Run the server                                                       |
| `./gradlew runDocker`         | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Architecture

The application follows a clean architecture approach with separation of concerns:

- **Routes**: Define API endpoints and handle HTTP requests/responses
- **Repositories**: Handle data access and storage
- **Domain Models**: Define the core business entities
- **Configuration**: Manage application settings and dependencies

## Metrics and Monitoring

The application includes comprehensive metrics collection and visualization:

### Available Metrics

- **HTTP Metrics**: Request counts, durations, and status codes
- **AI Metrics**: Question answering time and document loading time
- **Test Metrics**: Silly counter and random gauge for verifying Grafana functionality

### Monitoring Setup

The monitoring stack includes:

- **Prometheus**: Collects and stores metrics from the application
- **Grafana**: Visualizes metrics with pre-configured dashboards

### Accessing Dashboards

When running with Docker Compose:

1. Start the application and monitoring stack: `cd docker && docker-compose up -d`
2. Access Grafana at http://localhost:3000 (default credentials: admin/admin)
3. Access Prometheus at http://localhost:9090
4. View raw metrics at http://localhost:8080/metrics

The Grafana dashboard includes the following panels:
- AI Questions In Flight: Shows the number of AI questions currently being processed
- AI Question Answer Time: Shows the average time taken to answer questions
- Silly Counter: Shows the test counter that increments every second
- Silly Gauge: Shows the test gauge with random values

### Testing Grafana Functionality

The application includes built-in test metrics that generate data automatically:

- **Silly Counter**: Increments every second, providing a continuous stream of data
- **Random Gauge**: Updates with random values between 0-100 every second

These metrics should appear in the Grafana dashboard immediately after starting the application, allowing you to verify that the metrics collection and visualization pipeline is working correctly without needing to generate application traffic.

## Development Guidelines

### General Principles

- Write referentially transparent code
- Keep code concise and focused
- Follow idiomatic Kotlin practices
- Prioritize readability and maintainability
- Never leave unused imports
- Use List instead of MutableList, etc
- Use `val` instead of `var` by default
- Only use `var` when state mutation is necessary
- Use data classes for representing state
- Keep data classes focused and immutable
- Avoid throwing exceptions for control flow
- Prefer KotlinX libraries where available
- Maintain versions in the Version Catalog (libs.versions.toml)
- Use Kotlin Gradle Script (build.gradle.kts)
- Do not downgrade dependencies manually

### Architecture and Design

- Maintain a clear separation of concerns
- Use dependency injection where appropriate
- Follow structured concurrency (e.g., supervisorScope for fault isolation)
- Validate all external inputs (e.g., user data, API requests) to prevent injection attacks
- Avoid Thread.sleep(); use delay() instead
- Use copy() carefully to avoid unintended side effects in immutable structures
- Avoid hardcoding secrets (use environment variables)
- Leverage higher-order functions (e.g., map, filter) and avoid imperative loops where possible

### Testing

- Write unit tests for business logic
- Use Ktor's testing utilities for integration tests
- Follow TDD practices where possible
- Try to write as many failing tests as you can before fixing them
- Avoid writing many assertions in a test and try to keep the test small
- Use runBlocking only in tests, not production code

### Code Style

- Follow the official Kotlin Style Guide for consistency
- Don't write meaningless comments that express in natural text what is already clear from the function names or code
- Avoid excessive documentation comments that simply repeat property names and types

## Resources

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- [Exposed Documentation](https://github.com/JetBrains/Exposed/wiki)
- [Jakarta Validation](https://jakarta.ee/specifications/bean-validation/3.0/jakarta-bean-validation-spec-3.0.html)
- [LangChain4j Documentation](https://github.com/langchain4j/langchain4j)
- [Prometheus Documentation](https://prometheus.io/docs/introduction/overview/)
- [Grafana Documentation](https://grafana.com/docs/)
