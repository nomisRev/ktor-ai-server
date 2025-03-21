package org.jetbrains.ktor.sample.ai

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.put
import kotlinx.coroutines.Deferred

fun Routing.installAiRoutes(ai: Deferred<AiRepo>) {
    put("/ai") {
        val question = call.queryParameters["question"]!!
        val response = ai.await().answer(1, question)
        call.respond(HttpStatusCode.OK, response)
    }
}