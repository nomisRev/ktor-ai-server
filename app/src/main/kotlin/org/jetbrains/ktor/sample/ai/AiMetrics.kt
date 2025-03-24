package org.jetbrains.ktor.sample.ai

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.TimeSource

class AiMetrics(registry: MeterRegistry) {

    private val inflight = AtomicInteger(0)
    private val gauge = Gauge
        .builder("ai.question.answer.inflight") { inflight.get() }
        .description("Amount of inflight AI questions to answer")
        .register(registry)

    private val questionAnswerTimer = Timer
        .builder("ai.question.answer.time")
        .description("Time taken to answer a question")
        .register(registry)

    private val documentLoadTimer = Timer
        .builder("ai.document.load.time")
        .description("Time taken to load a document")
        .register(registry)

    fun <A> trackAiQuestion(block: () -> A): A {
        inflight.incrementAndGet()
        val mark = TimeSource.Monotonic.markNow()
        return try {
            block()
        } finally {
            questionAnswerTimer.record(mark.elapsedNow().inWholeMilliseconds, TimeUnit.MILLISECONDS)
            inflight.incrementAndGet()
        }
    }

    fun <A> measureDocumentLoadTime(block: () -> A): A {
        val mark = TimeSource.Monotonic.markNow()
        return try {
            block()
        } finally {
            documentLoadTimer.record(mark.elapsedNow().inWholeMilliseconds, TimeUnit.MILLISECONDS)
        }
    }
}
