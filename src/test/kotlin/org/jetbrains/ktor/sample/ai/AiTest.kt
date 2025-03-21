package org.jetbrains.ktor.sample.ai

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.jetbrains.ktor.sample.DatabaseSpec

class AiTest : DatabaseSpec() {
    private val memory by lazy { ExposedChatMemoryStore(database) }
    private val ai by lazy { AiRepo(config, memory, AiMetrics(PrometheusMeterRegistry((PrometheusConfig.DEFAULT)))) }

    private val config = AIConfig(
        "http://localhost:8080",
        "not-need-for-llama.cpp",
        "Qwen_QwQ-32B-Q4_K_M.gguf",
        "all-minilm-l6-v2-q-tokenizer.json",
        550,
        50
    )

    // @Test // Requires local llama.cpp to be running. TODO: test with Ollama/LMStudios
    fun test() {
        ai.loadTestDocuments()
        val answer = ai.answer(1L, "Tell me about **Alan Turing**")
        println(answer)
    }
}
