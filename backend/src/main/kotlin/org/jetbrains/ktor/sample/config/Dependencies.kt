package org.jetbrains.ktor.sample.config

import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import io.ktor.server.application.Application
import java.time.Duration
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.ktor.sample.ai.AiService
import org.jetbrains.ktor.sample.ai.DocumentService
import org.jetbrains.ktor.sample.ai.ExposedChatMemoryStore

class Dependencies(val ai: Deferred<AiService>, val documentService: Deferred<DocumentService>)

fun Application.dependencies(config: AppConfig): Dependencies {
    val database = setupDatabase(config.database, config.flyway)
    val registry = setupMetrics()
    val model: StreamingChatLanguageModel =
        OllamaStreamingChatModel.builder()
            .baseUrl(config.ai.baseUrl)
            .logRequests(true)
            .logResponses(true)
            .modelName(config.ai.model)
            .temperature(0.7)
            .timeout(Duration.ofMinutes(2))
            .build()

    val aiModule =
        async(Dispatchers.IO) { AiModule(config.ai, ExposedChatMemoryStore(database), model) }

    return Dependencies(
        ai = async(Dispatchers.IO) { AiService(aiModule.await(), registry) },
        documentService =
            async(Dispatchers.IO) { DocumentService(aiModule.await().ingestor, registry) },
    )
}
