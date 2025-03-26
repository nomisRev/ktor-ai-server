package org.jetbrains.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.chat.model.ChatState
import org.jetbrains.chat.model.Message
import org.jetbrains.chat.model.MessageStatus
import org.jetbrains.chat.model.Sender
import org.jetbrains.chat.repository.ChatRepository
import org.jetbrains.chat.repository.DemoChatRepository

class ChatViewModel(
    private val repository: ChatRepository = DemoChatRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ChatState>(ChatState.Idle(emptyList()))
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun sendMessage(text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return

        val userMessage = Message(
            content = trimmedText,
            sender = Sender.USER,
            status = MessageStatus.SENT
        )

        _state.update { ChatState.Loading(it.messages + userMessage) }

        viewModelScope.launch {
            try {
                repository.sendMessage(userMessage).collect { aiResponse ->
                    _state.update { ChatState.Idle(it.messages + aiResponse) }
                }
            } catch (e: Exception) {
                _state.update { ChatState.Error(it.messages, "Failed to get response: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { ChatState.Idle(it.messages) }
    }
}
