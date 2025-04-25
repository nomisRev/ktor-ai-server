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
import kotlin.test.assertNotNull
import org.jetbrains.ktor.sample.withApp

class CustomerRoutesTest {

    @Test
    fun `create customer returns 201 Created`() = withApp {
        // Create a customer
        val createResponse =
            post("/api/customers") {
                contentType(ContentType.Application.Json)
                setBody(CreateCustomer(name = "Test Customer", email = "test.customer@example.com"))
            }

        // Verify response
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val customer = createResponse.body<CustomerResponse>()
        assertNotNull(customer.id)
        assertEquals("Test Customer", customer.name)
        assertEquals("test.customer@example.com", customer.email)
        assertNotNull(customer.createdAt)
    }

    @Test
    fun `get customer returns 200 OK`() = withApp {
        // Create a customer first
        val createResponse =
            post("/api/customers") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateCustomer(
                        name = "Get Test Customer",
                        email = "get.test.customer@example.com",
                    )
                )
            }
        val createdCustomer = createResponse.body<CustomerResponse>()

        // Get the customer
        val getResponse = get("/api/customers/${createdCustomer.id}")

        // Verify response
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val customer = getResponse.body<CustomerResponse>()
        assertEquals(createdCustomer.id, customer.id)
        assertEquals("Get Test Customer", customer.name)
        assertEquals("get.test.customer@example.com", customer.email)
    }

    @Test
    fun `update customer returns 200 OK`() = withApp {
        // Create a customer first
        val createResponse =
            post("/api/customers") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateCustomer(
                        name = "Update Test Customer",
                        email = "update.test.customer@example.com",
                    )
                )
            }
        val createdCustomer = createResponse.body<CustomerResponse>()

        // Update the customer
        val updateResponse =
            put("/api/customers/${createdCustomer.id}") {
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateCustomer(
                        name = "Updated Customer",
                        email = "updated.customer@example.com",
                    )
                )
            }

        // Verify response
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updatedCustomer = updateResponse.body<CustomerResponse>()
        assertEquals(createdCustomer.id, updatedCustomer.id)
        assertEquals("Updated Customer", updatedCustomer.name)
        assertEquals("updated.customer@example.com", updatedCustomer.email)
    }

    @Test
    fun `delete customer returns 204 No Content`() = withApp {
        // Create a customer first
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
        val createdCustomer = createResponse.body<CustomerResponse>()

        // Delete the customer
        val deleteResponse = delete("/api/customers/${createdCustomer.id}")

        // Verify response
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        // Try to get the deleted customer
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
