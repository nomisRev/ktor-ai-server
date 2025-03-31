# LangChain4j Kotlin Coroutines Integration

This module provides integration between [LangChain4j](https://github.com/langchain4j/langchain4j) and Kotlin Coroutines, allowing you to use LangChain4j's streaming capabilities with Kotlin's Flow API.

## Features

- Convert LangChain4j's `TokenStream` to Kotlin's `Flow<String>`
- Convert LangChain4j's `TokenStream` to Kotlin's `Flow<StreamingChatLanguageModelReply>`
- Automatic integration with LangChain4j's service layer through SPI
- Seamless integration with Kotlin Coroutines ecosystem

## Usage

### Basic Usage

The module provides extension functions to convert a `TokenStream` to a `Flow`:

```kotlin
// Convert TokenStream to Flow<String>
val tokenStream: TokenStream = getTokenStream() // Get TokenStream from your model
val stringFlow = tokenStream.asFlow()

// Convert TokenStream to Flow<StreamingChatLanguageModelReply>
val replyFlow = tokenStream.asReplyFlow()
```

### Integration with LangChain4j Services

The module automatically integrates with LangChain4j's service layer, allowing you to define service interfaces that return `Flow` types:

```kotlin
// Define a service interface with methods that return Flow types
interface ChatService {
    @SystemMessage("You are a helpful assistant.")
    @UserMessage("{{question}}")
    fun answer(question: String): Flow<String>

    @SystemMessage("You are a helpful assistant.")
    @UserMessage("{{question}}")
    fun answerWithReplies(question: String): Flow<StreamingChatLanguageModelReply>
}

// Create an implementation of the service
val model = createStreamingChatLanguageModel() // Create your model
val chatService = AiServices.builder(ChatService::class.java)
    .streamingChatLanguageModel(model)
    .build()

// Use the service
val flow = chatService.answer("Tell me about Kotlin")
```

### Integration with Ktor WebSockets

The module works well with Ktor's WebSocket API for streaming responses to clients:

```kotlin
// Define a WebSocket endpoint that uses the ChatService
fun Routing.chatWebSocket(chatService: ChatService) {
    webSocket("/chat") {
        // Receive a question from the client
        val frame = incoming.receive() as Frame.Text
        val question = frame.readText()

        // Stream the answer back to the client
        chatService.answer(question)
            .collect { token ->
                outgoing.send(Frame.Text(token))
            }
    }
}
```

## How It Works

The module provides two main components:

1. Extension functions in `TokenStreamExt.kt` that convert `TokenStream` to `Flow`
2. Adapter classes that implement LangChain4j's `TokenStreamAdapter` SPI

When you define a service interface with methods that return `Flow<String>` or `Flow<StreamingChatLanguageModelReply>`, LangChain4j will use the appropriate adapter to convert the `TokenStream` to the requested `Flow` type.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](https://www.apache.org/licenses/LICENSE-2.0) file for details.
