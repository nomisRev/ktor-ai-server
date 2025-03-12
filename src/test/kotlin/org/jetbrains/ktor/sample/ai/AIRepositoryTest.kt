package org.jetbrains.ktor.sample.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * Integration tests for AIRepository with llama.cpp server running the Qwen_QwQ-32B-Q4_K_M.gguf model.
 * These tests require a running llama.cpp server.
 */
class AIRepositoryTest {

    private val config = AIConfig(
        baseUrl = "http://localhost:8080",
        apiKey = "not-needed-for-local-llama",
        model = "Qwen_QwQ-32B-Q4_K_M.gguf"
    )

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        engine {
            requestTimeout = 2.minutes.inWholeMilliseconds
        }
    }

    private val aiRepository = AIRepository(config, client)

    @Test
    fun `test basic chat completion`() = runBlocking {
        // Simple prompt to test basic functionality
        val response = aiRepository.getChatCompletion(
            prompt = "Hello, how are you?",
            temperature = 0.7
        )

        // Verify we got a non-empty response
        assertNotNull(response)
        assertTrue(response.isNotEmpty())
        println("[DEBUG_LOG] Basic chat completion response: $response")
    }

    @Test
    fun `test chat completion with system prompt`() = runBlocking {
        // Test with a system prompt to guide the model's behavior
        val response = aiRepository.getChatCompletion(
            prompt = "What is the capital of France?",
            systemPrompt = "You are a helpful assistant that provides concise, accurate answers.",
            temperature = 0.7
        )

        // Verify we got a non-empty response
        assertNotNull(response)
        assertTrue(response.isNotEmpty())
        assertTrue(response.contains("Paris", ignoreCase = true), 
            "Response should mention Paris as the capital of France")
        println("[DEBUG_LOG] Chat completion with system prompt response: $response")
    }

    @Test
    fun `test chat completion with technical question`() = runBlocking {
        // Test with a more complex, technical question
        val response = aiRepository.getChatCompletion(
            prompt = "Explain how transformers work in machine learning in 2-3 sentences.",
            systemPrompt = "You are an AI expert providing concise technical explanations.",
            temperature = 0.7
        )

        // Verify we got a non-empty response
        assertNotNull(response)
        assertTrue(response.isNotEmpty())
        println("[DEBUG_LOG] Technical question response: $response")
    }

    @Test
    fun `test chat completion with creative prompt`() = runBlocking {
        // Test with a creative prompt to evaluate the model's creative capabilities
        val response = aiRepository.getChatCompletion(
            prompt = "Write a short poem about programming in Kotlin.",
            systemPrompt = "You are a creative assistant that writes engaging, concise content.",
            temperature = 0.9  // Higher temperature for more creativity
        )

        // Verify we got a non-empty response
        assertNotNull(response)
        assertTrue(response.isNotEmpty())
        assertTrue(response.length > 20, "Response should be a substantial poem")
        println("[DEBUG_LOG] Creative prompt response: $response")
    }
}
