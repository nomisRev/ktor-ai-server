package org.jetbrains.ktor.sample.ai

import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.time.Duration
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.config.AIConfig
import org.jetbrains.ktor.sample.config.AiModule

class AiTest : DatabaseSpec() {
    private val config =
        AIConfig(
            "http://localhost:11434",
            "not-need-for-llama.cpp",
            "llama3.2",
            "all-minilm-l6-v2-q-tokenizer.json",
            550,
            50,
        )
    private val memory by lazy { ExposedChatMemoryStore(database) }
    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val module by lazy {
        AiModule(
            config,
            memory
        )
    }
    private val documents by lazy { DocumentService(module.ingestor, registry) }
    private val ai by lazy { AiService(module, registry) }

    @Test // Requires local Ollama to be running.
    fun test() = runBlocking {
        ai.answer("userId", "Tell me about **Alan Turing**").collect { println(it) }
    }
}
