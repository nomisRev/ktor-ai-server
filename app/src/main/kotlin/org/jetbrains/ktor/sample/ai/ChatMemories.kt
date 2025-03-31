package org.jetbrains.ktor.sample.ai

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.ChatMessageDeserializer
import dev.langchain4j.data.message.ChatMessageSerializer
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

object ChatMemories : LongIdTable("chat_memories", "memory_id") {
    val memoryKey = varchar("memory_key", 255).uniqueIndex()
    val messages = text("messages")
}

class ExposedChatMemoryStore(private val database: Database) : ChatMemoryStore {

    override fun getMessages(memoryId: Any): List<ChatMessage> {
        val key = memoryId.toString()
        val json = transaction(database) {
            ChatMemories.selectAll()
                .where { ChatMemories.memoryKey eq key }
                .map { it[ChatMemories.messages] }
                .singleOrNull()
        }
        return ChatMessageDeserializer.messagesFromJson(json)
    }

    override fun updateMessages(memoryId: Any?, messages: List<ChatMessage>) {
        val key = memoryId.toString()
        val json = ChatMessageSerializer.messagesToJson(messages)
        transaction(database) {
            ChatMemories.upsert(ChatMemories.memoryKey) {
                it[memoryKey] = key
                it[ChatMemories.messages] = json
            }
        }
    }

    override fun deleteMessages(memoryId: Any?) {
        val key = memoryId.toString()
        transaction(database) {
            ChatMemories.deleteWhere { memoryKey eq key }
        }
    }
}
