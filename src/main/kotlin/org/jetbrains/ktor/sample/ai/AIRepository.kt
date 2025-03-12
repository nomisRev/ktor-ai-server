package org.jetbrains.ktor.sample.ai

import io.github.nomisrev.openai.bodyOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

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

    suspend fun createAssistantWithFileAndRun(
        assistantName: String,
        assistantInstructions: String,
        filePath: String,
        prompt: String,
        runInstructions: String? = null
    ): CreateAssistantWithFileAndRunResponse {
        val file = File(filePath)
        val fileResponse = uploadFile(file, "assistants")

        val assistantResponse = createAssistant(
            name = assistantName,
            instructions = assistantInstructions,
            fileIds = listOf(fileResponse.id)
        )

        val threadResponse = createThread(listOf(ThreadMessage(role = "user", content = prompt)))

        val runResponse = createRun(
            threadId = threadResponse.id,
            assistantId = assistantResponse.id,
            instructions = runInstructions
        )

        return CreateAssistantWithFileAndRunResponse(
            assistant = assistantResponse,
            file = fileResponse,
            thread = threadResponse,
            run = runResponse
        )
    }

    private suspend fun uploadFile(file: File, purpose: String): FileUploadResponse =
        client.post("${config.baseUrl}/v1/files") {
            bearerAuth(config.apiKey)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("purpose", purpose)
                        append(
                            "file",
                            file.readBytes(),
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                                append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                            }
                        )
                    }
                )
            )
        }.body()

    private suspend fun createAssistant(
        name: String,
        instructions: String,
        fileIds: List<String>? = null
    ): AssistantResponse =
        client.post("${config.baseUrl}/v1/assistants") {
            contentType(ContentType.Application.Json)
            bearerAuth(config.apiKey)
            setBody(
                AssistantRequest(
                    model = config.model,
                    name = name,
                    instructions = instructions,
                    fileIds = fileIds
                )
            )
        }.body<AssistantResponse>()

    private suspend fun createThread(
        messages: List<ThreadMessage>? = null
    ): ThreadResponse =
        client.post("${config.baseUrl}/v1/threads") {
            contentType(ContentType.Application.Json)
            bearerAuth(config.apiKey)
            setBody(
                ThreadRequest(
                    messages = messages
                )
            )
        }.body<ThreadResponse>()

    private suspend fun createRun(
        threadId: String,
        assistantId: String,
        instructions: String? = null
    ): RunResponse =
        client.post("${config.baseUrl}/v1/threads/$threadId/runs") {
            contentType(ContentType.Application.Json)
            bearerAuth(config.apiKey)
            setBody(
                RunRequest(
                    assistantId = assistantId,
                    instructions = instructions
                )
            )
        }.body<RunResponse>()
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

// Assistant API data classes
@Serializable
data class AssistantRequest(
    val model: String,
    val name: String? = null,
    val description: String? = null,
    val instructions: String? = null,
    val tools: List<AssistantTool>? = null,
    @SerialName("file_ids") val fileIds: List<String>? = null
)

@Serializable
data class AssistantTool(
    val type: String
)

@Serializable
data class AssistantResponse(
    val id: String,
    @SerialName("object") val objectType: String,
    @SerialName("created_at") val createdAt: Long,
    val name: String?,
    val description: String?,
    val model: String,
    val instructions: String?,
    @SerialName("file_ids") val fileIds: List<String>?
)

@Serializable
data class FileUploadResponse(
    val id: String,
    @SerialName("object") val objectType: String,
    val bytes: Int,
    @SerialName("created_at") val createdAt: Long,
    val filename: String,
    val purpose: String
)

@Serializable
data class ThreadRequest(
    val messages: List<ThreadMessage>? = null
)

@Serializable
data class ThreadMessage(
    val role: String,
    val content: String,
    @SerialName("file_ids") val fileIds: List<String>? = null
)

@Serializable
data class ThreadResponse(
    val id: String,
    @SerialName("object") val objectType: String,
    @SerialName("created_at") val createdAt: Long
)

@Serializable
data class RunRequest(
    @SerialName("assistant_id") val assistantId: String,
    val instructions: String? = null
)

@Serializable
data class RunResponse(
    val id: String,
    @SerialName("object") val objectType: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("assistant_id") val assistantId: String,
    @SerialName("thread_id") val threadId: String,
    val status: String,
    @SerialName("required_action") val requiredAction: RequiredAction? = null,
    @SerialName("last_error") val lastError: LastError? = null
)

@Serializable
data class RequiredAction(
    val type: String,
    @SerialName("submit_tool_outputs") val submitToolOutputs: SubmitToolOutputs? = null
)

@Serializable
data class SubmitToolOutputs(
    @SerialName("tool_calls") val toolCalls: List<ToolCall>
)

@Serializable
data class ToolCall(
    val id: String,
    val type: String,
    val function: ToolCallFunction
)

@Serializable
data class ToolCallFunction(
    val name: String,
    val arguments: String
)

@Serializable
data class LastError(
    val code: String,
    val message: String
)

@Serializable
data class AssistantFileUploadRequest(
    @SerialName("file_id") val fileId: String
)

@Serializable
data class AssistantFileResponse(
    val id: String,
    @SerialName("object") val objectType: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("assistant_id") val assistantId: String
)

@Serializable
data class CreateAssistantWithFileAndRunResponse(
    val assistant: AssistantResponse,
    val file: FileUploadResponse,
    val thread: ThreadResponse,
    val run: RunResponse
)
