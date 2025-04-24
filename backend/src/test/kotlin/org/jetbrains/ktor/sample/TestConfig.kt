package org.jetbrains.ktor.sample

import org.jetbrains.ktor.sample.config.AIConfig
import org.jetbrains.ktor.sample.config.AppConfig
import org.jetbrains.ktor.sample.config.FlywayConfig
import org.jetbrains.ktor.sample.security.AuthConfig

val AppTestConfig by lazy {
    AppConfig(
        database = PostgresContainer.getDatabaseConfig(),
        flyway = FlywayConfig(locations = "classpath:db/migration", baselineOnMigrate = true),
        auth =
            AuthConfig(
                authorizeUrl = "http://localhost:8080/oauth2/authorize",
                accessTokenUrl = "http://localhost:8080/oauth2/token",
                clientId = "ktor-sample-app",
                clientSecret = "secret",
                redirectUrl = "http://localhost:8080/login/oauth2/code/google",
                encryptionKey = "secret",
                signKey = "secret",
            ),
        ai =
            AIConfig(
                "http://localhost:8080",
                "not-need-for-llama.cpp",
                "Qwen_QwQ-32B-Q4_K_M.gguf",
                "all-minilm-l6-v2-q-tokenizer.json",
                550,
                50,
            ),
    )
}
