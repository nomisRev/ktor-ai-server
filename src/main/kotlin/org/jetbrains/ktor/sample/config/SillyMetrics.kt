package org.jetbrains.ktor.sample.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.ktor.server.application.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Simulate some events for a Gauge and a Counter to integration test Prometheus, and Grafana
 */
fun Application.setupSillyMetrics(registry: MeterRegistry) {
    val sillyCounter = Counter
        .builder("silly.counter")
        .description("A silly counter that increments randomly")
        .register(registry)

    var randomGaugeValue = 0.0

    Gauge
        .builder("silly.gauge", this) { randomGaugeValue }
        .description("A silly gauge that shows random values")
        .register(registry)

    launch {
        while (isActive) {
            sillyCounter.increment()
            randomGaugeValue++
            delay(1000) // Update every second
        }
    }
}