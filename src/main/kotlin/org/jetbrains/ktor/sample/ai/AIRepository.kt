package org.jetbrains.ktor.sample.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AIRepository(
    private val config: AIConfig,
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) : AutoCloseable by client {

    suspend fun getChatCompletion(
        prompt: String,
        systemPrompt: String? = null,
        temperature: Double = 0.7
    ): String? {
        val messages = buildList {
            if (systemPrompt != null) add(ChatMessage("system", systemPrompt))
            add(ChatMessage("user", prompt))
        }

        return client.post("${config.baseUrl}/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            bearerAuth(config.apiKey)
            setBody(
                ChatCompletionRequest(
                    model = config.model,
                    messages = messages,
                    temperature = temperature
                )
            )
        }.body<ChatCompletionResponse>()
            .choices
            .firstOrNull()
            ?.message
            ?.content
    }
}

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    @SerialName("object") val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason") val finishReason: String
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)
