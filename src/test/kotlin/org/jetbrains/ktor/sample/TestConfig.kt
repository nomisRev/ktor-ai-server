package org.jetbrains.ktor.sample

import org.jetbrains.ktor.sample.ai.AIConfig
import org.jetbrains.ktor.sample.auth.JWTConfig
import org.jetbrains.ktor.sample.users.Argon2HasherConfig

val AppTestConfig by lazy {
    AppConfig(
        jwt = JWTConfig(
            issuer = "test-issuer",
            audience = "test-audience",
            realm = "test-realm",
            secret = "test-secret-key-that-is-long-enough-for-testing"
        ),
        database = PostgresContainer.getDatabaseConfig(),
        flyway = FlywayConfig(
            locations = "classpath:db/migration",
            baselineOnMigrate = true
        ),
        ai = AIConfig(
            "http://localhost:8080",
            "not-need-for-llama.cpp",
            "Qwen_QwQ-32B-Q4_K_M.gguf",
            "all-minilm-l6-v2-q-tokenizer.json",
            550,
            50
        ),
        argon2 = Argon2HasherConfig(
            memory = 65536,
            iterations = 1,
            parallelism = 4,
            outputLength = 32,
            limitedParallelism = 4
        )
    )
}
