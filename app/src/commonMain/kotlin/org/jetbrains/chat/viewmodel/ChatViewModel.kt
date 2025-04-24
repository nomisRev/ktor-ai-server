package org.jetbrains.chat.viewmodel

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.chat.repository.ChatRepository

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalUuidApi::class)
data class Message(
    val id: String = Uuid.random().toString(),
    val content: String,
    val type: MessageType,
    val timestamp: Instant = Clock.System.now(),
    val isComplete: Boolean = true,
)

enum class MessageType {
    USER,
    AI,
}

class ChatViewModel(private val repository: ChatRepository, private val scope: CoroutineScope) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun connect(): Job =
        repository.connect().map { message -> processIncomingMessage(message) }.launchIn(scope)

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        val messages =
            listOf(
                Message(content = content, type = MessageType.USER),
                Message(content = "", type = MessageType.AI, isComplete = false),
            )
        _state.update { it.copy(messages = it.messages + messages, isLoading = true) }
        repository.sendMessage(content)
    }

    private fun processIncomingMessage(text: String) {
        _state.update { state ->
            val lastMessageOrNull = state.messages.lastOrNull()
            if (lastMessageOrNull?.type == MessageType.AI) {
                val isComplete = text == "### END ###"
                val updatedContent =
                    if (isComplete) lastMessageOrNull.content else lastMessageOrNull.content + text

                state.copy(
                    messages =
                        state.messages.dropLast(1) +
                            lastMessageOrNull.copy(content = updatedContent, isComplete = true),
                    isLoading = false,
                )
            } else {
                val newMessage = Message(content = text, type = MessageType.AI, isComplete = false)
                state.copy(messages = state.messages + newMessage)
            }
        }
    }
}
