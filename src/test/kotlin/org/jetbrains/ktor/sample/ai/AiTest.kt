package org.jetbrains.ktor.sample.ai

import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.ai.memory.ExposedChatMemoryStore

class AiTest : DatabaseSpec() {
    private val memory by lazy { ExposedChatMemoryStore(database) }
    private val ai by lazy { AiRepo(config, memory) }

    private val config = AIConfig(
        "http://localhost:8080",
        "not-need-for-llama.cpp",
        "Qwen_QwQ-32B-Q4_K_M.gguf",
        "all-minilm-l6-v2-q-tokenizer.json",
        550,
        50
    )

    //    @Test
    fun test() {
        ai.loadDocuments()
        val answer = ai.answer(1L, "Tell me about **Alan Turing**")
        println(answer)
    }
}
