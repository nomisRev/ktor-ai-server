package com.example

import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.ktor.server.application.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigTest {
    @Test
    fun `test loading configuration`() {
        val environment = createTestEnvironment {
            config = MapApplicationConfig(
                "jwt.domain" to "https://test-auth-domain/",
                "jwt.audience" to "test-audience",
                "jwt.realm" to "test realm",
                "jwt.secret" to "test-secret"
            )
        }

        val jwt = AppConfig.load(environment).jwt

        assertEquals("https://test-auth-domain/", jwt.domain)
        assertEquals("test-audience", jwt.audience)
        assertEquals("test realm", jwt.realm)
        assertEquals("test-secret", jwt.secret)
    }
}
