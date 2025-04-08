package org.jetbrains.ktor.sample.config

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun Application.setupMetrics(): PrometheusMeterRegistry {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) { registry = prometheus }
    routing { get("/metrics") { call.respond(prometheus.scrape()) } }
    setupSillyMetrics(prometheus)
    monitor.subscribe(ApplicationStopped) { prometheus.close() }
    return prometheus
}

// Simulate some events for a Gauge and a Counter to integration test Prometheus, and Grafana
private fun Application.setupSillyMetrics(registry: MeterRegistry) {
    val sillyCounter =
        Counter.builder("silly.counter")
            .description("A silly counter that increments randomly")
            .register(registry)

    var randomGaugeValue = 0.0

    Gauge.builder("silly.gauge", this) { randomGaugeValue }
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
