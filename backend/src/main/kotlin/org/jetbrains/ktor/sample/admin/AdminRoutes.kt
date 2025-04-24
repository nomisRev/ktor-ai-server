package org.jetbrains.ktor.sample.admin

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.asFlow
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyTo
import java.io.File
import kotlin.io.path.createTempFile
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import org.jetbrains.ktor.sample.ai.DocumentService

@Serializable data class DocumentUpload(val content: String)

fun Routing.installAdminRoutes(documents: Deferred<DocumentService>) {
    authenticate {
        post("/admin/documents/upload") {
            val upload = call.receive<DocumentUpload>()
            documents.await().ingestDocument(upload.content)
            call.respond(HttpStatusCode.Created)
        }

        // TODO: proper error handling
        post("/admin/documents/upload-pdf") {
            val pdfs = call.receiveMultipart().asFlow().flatMapConcat { part -> parseFiles(part) }
            documents.await().ingestPdfs(pdfs).collect()
            call.respond(HttpStatusCode.Created)
        }
    }
}

private fun parseFiles(part: PartData): Flow<File> =
    if (part !is PartData.FileItem) emptyFlow()
    else {
        flow {
            val tmp = createTempFile(part.originalFileName, ".pdf").toFile()
            try {
                val readChannel = part.provider()
                readChannel.copyTo(tmp.writeChannel())
                emit(tmp)
            } finally {
                tmp.delete()
            }
        }
    }
