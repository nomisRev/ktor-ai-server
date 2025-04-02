# Ktor AI Travel Assistant

An AI-powered travel assistant chat application built with Ktor and Kotlin Multiplatform. This application provides personalized travel recommendations, information, and advice through a conversational interface.

## Overview

This project demonstrates how to build a modern AI-powered chat application using Kotlin and Ktor. It features:

- A travel agency assistant that provides personalized travel recommendations
- Real-time chat interface using WebSockets and Server-Sent Events (SSE)
- AI-powered responses using OpenAI models via LangChain4j
- Retrieval Augmented Generation (RAG) for enhanced responses with domain-specific knowledge
- Conversation memory to maintain context across messages
- OAuth authentication (with Keycloak)
- Metrics tracking for AI question answering

## Technologies

### Backend
- **Ktor**: Kotlin asynchronous web framework
- **LangChain4j**: Java/Kotlin library for working with large language models
- **OpenAI**: AI models for generating responses
- **Exposed**: Kotlin SQL framework for database access
- **Flyway**: Database migration tool
- **Kotlinx.serialization**: JSON serialization/deserialization
- **WebSockets & SSE**: For real-time communication
- **Keycloak**: For OAuth authentication and authorization

### Frontend
- **Kotlin Multiplatform**: For sharing code between platforms
- **Compose Multiplatform**: For cross-platform UI development
- **Ktor Client**: For WebSocket and HTTP communication with the server

### DevOps
- **Docker**: For containerization and deployment
- **Gradle**: For build automation
- **Grafana**: For metrics visualization
- **PostgreSQL**: For data storage
- **Nginx**: For reverse proxy and routing

## Architecture

The application follows a modular architecture:

1. **AI Module**: Handles interaction with OpenAI models and document retrieval
2. **Chat Module**: Manages WebSocket/SSE connections and message routing
3. **Security Module**: Handles OAuth authentication with Keycloak
4. **Admin Module**: Provides administrative functions for document management
5. **ComposeApp Module**: Implements the cross-platform user interface

The application uses Retrieval Augmented Generation (RAG) to enhance AI responses with domain-specific knowledge. It can ingest documents to build a knowledge base that the AI can reference when answering questions.

## Project Structure

The project follows a multi-module setup:
- `app`: Main server application with Ktor
- `composeApp`: Cross-platform UI using Compose Multiplatform
- `langchain4j-kotlinx-coroutines`: Custom extensions for LangChain4j with Kotlin coroutines support

The project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies.

## Setup Instructions

### Prerequisites
- JDK 17 or higher
- Docker and Docker Compose

### Building the Project
```bash
./gradlew build
```

### Database Setup
The database is automatically set up when running with Docker Compose. The application uses Flyway to manage database migrations.

## Usage Instructions

### Running Locally
1. Start the Docker services:
```bash
cd docker
docker-compose up -d
```

2. Run the server:
```bash
./gradlew run
```

### Accessing the UI
- The UI is available at http://localhost
- Login with the credentials configured in Keycloak (user: ktor_user, pw: ktor_password)

### Example Interactions
You can ask the travel assistant questions like:
- "What are some good destinations for a family vacation in Europe?"
- "I'm planning a trip to Japan in April. What should I know?"
- "What's the best way to travel between Paris and London?"

## Deployment Instructions

### Docker Setup
The application can be deployed using Docker Compose:
```bash
cd docker
docker-compose up -d
```

This will start:
- PostgreSQL database
- Grafana for monitoring
- Keycloak for authentication
- Nginx as a reverse proxy

### Configuration Options
Configuration options can be modified in:
- `app/src/main/resources/application.yaml`
- `docker/docker-compose.yml`
- `docker/nginx.conf`

## Development

### Testing
- Relies on TestContainers to start Postgres
```bash
./gradlew test
```

### Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License
This project is licensed under the Apache 2.0 License - see the LICENSE file for details.
