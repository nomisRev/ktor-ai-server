package org.jetbrains.chat.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random


data class Message(
    val content: String,
    val sender: Sender,
    val timestamp: Instant = Clock.System.now(),
    val status: MessageStatus = MessageStatus.SENT
)

enum class Sender {
    USER,
    AI
}

enum class MessageStatus {
    SENDING,
    SENT,
    ERROR
}
