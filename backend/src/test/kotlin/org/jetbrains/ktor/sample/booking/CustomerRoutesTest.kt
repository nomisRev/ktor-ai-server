package org.jetbrains.ktor.sample.booking

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.ktor.sample.withApp

class CustomerRoutesTest {

    @Test
    fun `create customer returns 201 Created`() = withApp {
        val create = CreateCustomer(name = "Test Customer", email = "test.customer@example.com")
        val createResponse =
            post("/api/customers") {
                contentType(ContentType.Application.Json)
                setBody(create)
            }

        assertEquals(HttpStatusCode.Created, createResponse.status)

        val customer = createResponse.body<Customer>()
        assertEquals(create.name, customer.name)
        assertEquals(create.email, customer.email)
    }

    @Test
    fun `get customer returns 200 OK`() = withApp {
        val create =
            CreateCustomer(name = "Get Test Customer", email = "get.test.customer@example.com")
        val createdCustomer =
            post("/api/customers") {
                    contentType(ContentType.Application.Json)
                    setBody(create)
                }
                .body<Customer>()

        val getResponse = get("/api/customers/${createdCustomer.id}")

        assertEquals(HttpStatusCode.OK, getResponse.status)

        val customer = getResponse.body<Customer>()
        assertEquals(createdCustomer.id, customer.id)
        assertEquals(create.name, customer.name)
        assertEquals(create.email, customer.email)
    }

    @Test
    fun `update customer returns 200 OK`() = withApp {
        val createdCustomer =
            post("/api/customers") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        CreateCustomer(
                            name = "Update Test Customer",
                            email = "update.test.customer@example.com",
                        )
                    )
                }
                .body<Customer>()

        val update =
            UpdateCustomer(name = "Updated Customer", email = "updated.customer@example.com")
        val updateResponse =
            put("/api/customers/${createdCustomer.id}") {
                contentType(ContentType.Application.Json)
                setBody(update)
            }

        assertEquals(HttpStatusCode.OK, updateResponse.status)

        val updatedCustomer = updateResponse.body<Customer>()
        assertEquals(createdCustomer.id, updatedCustomer.id)
        assertEquals(update.name, updatedCustomer.name)
        assertEquals(update.email, updatedCustomer.email)
    }

    @Test
    fun `delete customer returns 204 No Content`() = withApp {
        val createResponse =
            post("/api/customers") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateCustomer(
                        name = "Delete Test Customer",
                        email = "delete.test.customer@example.com",
                    )
                )
            }

        val createdCustomer = createResponse.body<Customer>()

        val deleteResponse = delete("/api/customers/${createdCustomer.id}")

        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val getResponse = get("/api/customers/${createdCustomer.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `get non-existent customer returns 404 Not Found`() = withApp {
        val response = get("/api/customers/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `update non-existent customer returns 404 Not Found`() = withApp {
        val response =
            put("/api/customers/9999") {
                contentType(ContentType.Application.Json)
                setBody(UpdateCustomer(name = "Non-existent"))
            }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `delete non-existent customer returns 404 Not Found`() = withApp {
        val response = delete("/api/customers/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
