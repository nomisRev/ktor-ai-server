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

/**
 * Represents who sent a message.
 */
enum class Sender {
    USER,
    AI
}

/**
 * Represents the current status of a message.
 */
enum class MessageStatus {
    SENDING,
    SENT,
    ERROR
}
