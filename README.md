# Ktor AI Travel Assistant

AnAI-powered travel assistant chat application built with Ktor and Kotlin Multiplatform. This application provides personalized travel recommendations, information, and advice through a conversational interface.

## Overview

This project demonstrates how to build a modern AI-powered chat application using Kotlin and Ktor. It features:

- A travel agency assistant that provides personalized travel recommendations
- Real-time chat interface using WebSockets
- AI-powered responses using OpenAI models
- Retrieval Augmented Generation (RAG) for enhanced responses with domain-specific knowledge
- Conversation memory to maintain context across messages

## Technologies

### Backend
- **Ktor**: Kotlin asynchronous web framework
- **LangChain4j**: Java/Kotlin library for working with large language models
- **OpenAI**: AI models for generating responses
- **Exposed**: Kotlin SQL framework for database access
- **Flyway**: Database migration tool
- **Kotlinx.serialization**: JSON serialization/deserialization
- **WebSockets**: For real-time communication
- **JWT**: For authentication and authorization

### Frontend
- **Kotlin Multiplatform**: For sharing code between frontend and backend
- **Kotlin/JS**: For browser-based UI
- **Ktor Client**: For WebSocket communication with the server

### DevOps
- **Docker**: For containerization and deployment
- **Gradle**: For build automation
- **Prometheus**: For metrics and monitoring

## Architecture

The application follows a modular architecture:

1. **AI Module**: Handles interaction with OpenAI models and document retrieval
2. **Chat Module**: Manages WebSocket connections and message routing
3. **User Module**: Handles user authentication and session management
4. **Admin Module**: Provides administrative functions
5. **Frontend Module**: Implements the user interface

The application uses Retrieval Augmented Generation (RAG) to enhance AI responses with domain-specific knowledge. It can ingest documents to build a knowledge base that the AI can reference when answering questions.

## Project Structure

The project follows a multi-module setup:
- `app`: Main server application
- `frontend`: Browser-based user interface

The project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies.
