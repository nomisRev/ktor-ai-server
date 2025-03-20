package org.jetbrains.ktor.sample.ai

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit
import kotlin.time.TimeSource

class AiMetrics(registry: MeterRegistry) {

    private val questionAnswerTimer = Timer
        .builder("ai.question.answer.time")
        .description("Time taken to answer a question")
        .register(registry)

    private val documentLoadTimer = Timer
        .builder("ai.document.load.time")
        .description("Time taken to load a document")
        .register(registry)

    fun <A> measureQuestionAnswerTime(block: () -> A): A {
        val mark = TimeSource.Monotonic.markNow()
        try {
            return block()
        } finally {
            questionAnswerTimer.record(mark.elapsedNow().inWholeMilliseconds, TimeUnit.MILLISECONDS)
        }
    }

    fun <A> measureDocumentLoadTime(block: () -> A): A {
        val mark = TimeSource.Monotonic.markNow()
        try {
            return block()
        } finally {
            documentLoadTimer.record(mark.elapsedNow().inWholeMilliseconds, TimeUnit.MILLISECONDS)
        }
    }
}
