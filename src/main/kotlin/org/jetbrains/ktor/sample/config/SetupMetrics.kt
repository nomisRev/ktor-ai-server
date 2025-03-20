package org.jetbrains.ktor.sample.config

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.setupMetrics(): PrometheusMeterRegistry {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = prometheus
    }
    routing {
        get("/metrics") {
            call.respond(prometheus.scrape())
        }
    }
    monitor.subscribe(ApplicationStopped) { prometheus.close() }
    return prometheus
}