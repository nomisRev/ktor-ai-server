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

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun updateInputText(text: String) {
        _state.update { it.updateInputText(text) }
    }

    fun sendMessage() {
        val currentState = _state.value
        val inputText = currentState.inputText.trim()

        if (inputText.isEmpty()) return

        // Create user message
        val userMessage = Message(
            content = inputText,
            sender = Sender.USER,
            status = MessageStatus.SENT
        )

        _state.update {
            it.addMessage(userMessage).clearInputText().loading()
        }

        // Process AI response
        viewModelScope.launch {
            try {
                repository.sendMessage(userMessage).collect { aiResponse ->
                    _state.update { it.addMessage(aiResponse) }
                }
            } catch (e: Exception) {
                _state.update { it.withError("Failed to get response: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}