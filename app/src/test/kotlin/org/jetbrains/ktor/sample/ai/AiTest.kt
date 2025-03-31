package org.jetbrains.ktor.sample.ai

import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.runBlocking
import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.config.AIConfig
import org.jetbrains.ktor.sample.config.AiModule
import java.time.Duration
import kotlin.test.Test

class AiTest : DatabaseSpec() {
    private val config = AIConfig(
        "http://localhost:11434",
        "not-need-for-llama.cpp",
        "llama3.2",
        "all-minilm-l6-v2-q-tokenizer.json",
        550,
        50
    )
    private val memory by lazy { ExposedChatMemoryStore(database) }
    private val module by lazy {
        AiModule(
            config, memory, OllamaStreamingChatModel.builder()
                .baseUrl(config.baseUrl)
                .modelName(config.model)
                .temperature(0.7)
                .timeout(Duration.ofMinutes(2))
                .build()
        )
    }
    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val documents by lazy { DocumentService(module.ingestor, registry) }
    private val ai by lazy { AiService(module, registry) }

    @Test // Requires local llama.cpp to be running.
    fun test() = runBlocking {
        documents.loadTestDocuments()
        ai.answer(1L, "Tell me about **Alan Turing**")
            .collect { println(it) }
    }
}
