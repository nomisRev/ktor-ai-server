package org.jetbrains.ktor.sample.admin

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.jetbrains.ktor.sample.withApp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi

@ExperimentalUuidApi
class AdminRoutesTest {

    @Test
    fun `admin can upload document`() = withApp {
        val response = post("/admin/documents/upload") {
            contentType(ContentType.Application.Json)
            setBody(DocumentUpload("This is a test document content"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }
}
