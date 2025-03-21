package org.jetbrains.ktor.sample.admin

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import org.jetbrains.ktor.sample.ai.AiRepo
import org.jetbrains.ktor.sample.security.Role
import org.jetbrains.ktor.sample.security.authorized

@Serializable
data class DocumentUpload(val content: String)

fun Routing.installAdminRoutes(aiRepo: Deferred<AiRepo>) {
    authenticate {
        authorized(Role.ADMIN) {
            post("/admin/documents/upload") {
                val upload = call.receive<DocumentUpload>()
                aiRepo.await().ingestDocument(upload.content)
                call.respond(HttpStatusCode.Created)
            }
        }
    }
}
