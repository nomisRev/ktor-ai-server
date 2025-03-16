package org.jetbrains.ktor.sample.ai

import io.ktor.server.application.ApplicationEnvironment

data class AIConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String
) {
    companion object {
        fun load(environment: ApplicationEnvironment): AIConfig = with(environment.config) {
            AIConfig(
                baseUrl = property("ai.baseUrl").getString(),
                apiKey = property("ai.apiKey").getString(),
                model = property("ai.model").getString(),
            )
        }
    }
}