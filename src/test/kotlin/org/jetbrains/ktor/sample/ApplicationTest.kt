package org.jetbrains.ktor.sample

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testHelloWorld() = withApp {
        val response = get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, World!", response.bodyAsText())
    }
}
