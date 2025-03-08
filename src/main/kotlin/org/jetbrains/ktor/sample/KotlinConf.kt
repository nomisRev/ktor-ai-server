package org.jetbrains.ktor.sample

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

fun Application.module2() {
    launch {
        println("Application started")
        try {
            awaitCancellation()
        } catch (e: CancellationException) {
            println("Application cancelled.. sleeping..")
            withContext(NonCancellable) { delay(1000) }
            println("Application cancelled.. sleeping done.")
            throw e
        }
    }
    launch { streamEvents().collect(::println) }
}

fun streamEvents(): Flow<String> =
    flowOf("one", "two", "three")
