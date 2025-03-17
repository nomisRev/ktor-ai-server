package org.jetbrains.ktor.sample.ai

class AiTest {

    val config = AIConfig(
        "http://localhost:8080",
        "not-need-for-llama.cpp",
        "Qwen_QwQ-32B-Q4_K_M.gguf"
    )

    val ai = AiRepo(config)

    //    @Test
    fun test() {
        ai.loadDocuments()
        val answer = ai.answer("Tell me about **Alan Turing**")
        println(answer)
    }
}