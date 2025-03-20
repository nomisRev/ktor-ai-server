package org.jetbrains.ktor.sample.ai

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.UserMessage
import org.jetbrains.ktor.sample.DatabaseSpec
import org.junit.Test
import kotlin.test.assertEquals

class PersistentChatMemoryStoreTest : DatabaseSpec() {
    private val memoryStore by lazy { ExposedChatMemoryStore(database) }

    @Test
    fun `test store and retrieve messages`() {
        val memoryId = "test-memory-id"
        val messages = listOf(UserMessage("Hello"), AiMessage("Hi there!"))

        memoryStore.updateMessages(memoryId, messages)
        val retrievedMessages = memoryStore.getMessages(memoryId)

        assertEquals(messages, retrievedMessages)
    }

    @Test
    fun `test update existing messages`() {
        val memoryId = "test-memory-id-2"
        val initialMessages = listOf(UserMessage("Initial message"))
        val updatedMessages = initialMessages + listOf(AiMessage("Response"), UserMessage("Follow-up question"))

        memoryStore.updateMessages(memoryId, initialMessages)
        memoryStore.updateMessages(memoryId, updatedMessages)
        val retrievedMessages = memoryStore.getMessages(memoryId)

        assertEquals(updatedMessages, retrievedMessages)
    }

    @Test
    fun `test delete messages`() {
        val memoryId = "test-memory-id-3"
        val messages = listOf(UserMessage("Message to delete"))

        memoryStore.updateMessages(memoryId, messages)
        memoryStore.deleteMessages(memoryId)
        val retrievedMessages = memoryStore.getMessages(memoryId)

        assertEquals(emptyList(), retrievedMessages)
    }
}