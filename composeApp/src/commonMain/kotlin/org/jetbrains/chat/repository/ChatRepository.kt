package org.jetbrains.chat.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.chat.model.Message
import org.jetbrains.chat.model.Sender
import kotlin.random.Random

/**
 * Interface for chat repository operations.
 */
interface ChatRepository {
    /**
     * Sends a message and returns a flow of the AI response.
     */
    fun sendMessage(message: Message): Flow<Message>
}

/**
 * Simple implementation of ChatRepository for demo purposes.
 * This implementation simulates AI responses with predefined messages.
 */
class DemoChatRepository : ChatRepository {
    private val aiResponses = listOf(
        "Hello! How can I help you today?",
        "That's an interesting question. Let me think about it...",
        "I'm an AI assistant. I can help you with various tasks.",
        "Could you provide more details about your question?",
        "I'm here to assist you with any information you need.",
        "That's a great point! Let me add some thoughts...",
        "I understand your question. Here's what I think...",
        "Let me search for that information for you.",
        "I'm processing your request. Just a moment please.",
        "Thanks for sharing that. Here's my response..."
    )

    override fun sendMessage(message: Message): Flow<Message> = flow {
        // Simulate network delay
        delay(1000)
        
        // Randomly select an AI response
        val responseText = aiResponses[Random.nextInt(aiResponses.size)]
        
        // Create and emit the AI response message
        val responseMessage = Message(
            content = responseText,
            sender = Sender.AI
        )
        
        emit(responseMessage)
    }
}