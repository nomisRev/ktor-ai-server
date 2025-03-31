package org.jetbrains.ktor.sample.ai

import dev.langchain4j.model.chat.StreamingChatLanguageModelReply
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.ktor.sample.config.AiModule
import org.jetbrains.ktor.sample.config.services
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.TimeSource

private const val SYSTEM_MESSAGE: String =
    """You are an AI assistant for a travel agency. Your role is to provide helpful, accurate, 
and personalized travel information to customers. You should:

1. Provide detailed information about destinations, accommodations, transportation options, 
   and activities based on the customer's interests and preferences.
2. Consider factors like budget, travel dates, group size, and special requirements when 
   making recommendations.
3. Offer practical travel tips and advice relevant to the destinations being discussed.
4. Be knowledgeable about travel regulations, visa requirements, and safety considerations.
5. Maintain a friendly, professional tone that inspires confidence in your recommendations.
"""

class AiService(config: AiModule, registry: MeterRegistry) {
    private val chat: Chat = config.services<Chat>()
    private val inflight = AtomicInteger(0)
    private val gauge = Gauge
        .builder("ai.question.answer.inflight") { inflight.get() }
        .description("Amount of inflight AI questions to answer")
        .register(registry)

    private val questionAnswerTimer = Timer
        .builder("ai.question.answer.time")
        .description("Time taken to answer a question")
        .register(registry)

    fun answer(userId: Long, question: String): Flow<String> =
        chat.answer(userId, question)
            .track(::trackAiQuestion)

    suspend fun <A> trackAiQuestion(block: suspend () -> A): A {
        inflight.incrementAndGet()
        val mark = TimeSource.Monotonic.markNow()
        return try {
            block()
        } finally {
            questionAnswerTimer.record(mark.elapsedNow().inWholeMilliseconds, TimeUnit.MILLISECONDS)
            inflight.incrementAndGet()
        }
    }

    private interface Chat {
        @SystemMessage(SYSTEM_MESSAGE)
        @UserMessage("{{question}}")
        fun answer(@MemoryId userId: Long, @V("question") question: String): Flow<String>

        @SystemMessage(SYSTEM_MESSAGE)
        @UserMessage("{{question}}")
        fun example(@MemoryId userId: Long, @V("question") question: String): Flow<StreamingChatLanguageModelReply>
    }
}

/**
 * Wraps the [Flow] with builder methods like `trackAiQuestion { }, or `measureTimeMillis { }`
 */
private fun <A> Flow<A>.track(tracker: suspend (suspend () -> Unit) -> Unit): Flow<A> =
    flow {
        tracker {
            collect { emit(it) }
        }
    }
