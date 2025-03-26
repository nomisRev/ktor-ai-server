package org.jetbrains.chat.model

sealed interface ChatState {
    val messages: List<Message>

    data class Idle(override val messages: List<Message>) : ChatState
    data class Loading(override val messages: List<Message>) : ChatState
    data class Error(override val messages: List<Message>, val error: String) : ChatState

    fun loading(message: Message) = Loading(messages + message)
}
