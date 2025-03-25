package org.jetbrains.chat.model

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
) {
    fun addMessage(message: Message): ChatState =
        copy(messages = messages + message, isLoading = false, error = null)

    fun withError(errorMessage: String): ChatState =
        copy(error = errorMessage, isLoading = false)

    fun loading(): ChatState =
        copy(isLoading = true, error = null)

    fun updateInputText(text: String): ChatState =
        copy(inputText = text)

    fun clearInputText(): ChatState =
        copy(inputText = "")
}