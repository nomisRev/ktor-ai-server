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
import kotlin.test.assertTrue
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.ktor.sample.withApp

class BookingRoutesTest {

    // Helper function to create a test customer
    private suspend fun createTestCustomer(client: io.ktor.client.HttpClient): CustomerResponse {
        val response =
            client.post("/api/customers") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateCustomer(
                        name = "Booking Test Customer",
                        email = "booking.test.customer@example.com",
                    )
                )
            }
        return response.body()
    }

    @Test
    fun `create booking returns 201 Created`() = withApp {
        // Create a customer first
        val customer = createTestCustomer(this)

        // Create a booking
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val createResponse =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateBooking(
                        customerId = customer.id,
                        bookingDate = bookingDate,
                        amount = "100.50",
                    )
                )
            }

        // Verify response
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val booking = createResponse.body<BookingResponse>()
        assertNotNull(booking.id)
        assertEquals(customer.id, booking.customerId)
        assertEquals(bookingDate, booking.bookingDate)
        assertEquals("100.50", booking.amount)
    }

    @Test
    fun `get booking returns 200 OK`() = withApp {
        // Create a customer first
        val customer = createTestCustomer(this)

        // Create a booking
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val createResponse =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateBooking(
                        customerId = customer.id,
                        bookingDate = bookingDate,
                        amount = "200.75",
                    )
                )
            }
        val createdBooking = createResponse.body<BookingResponse>()

        // Get the booking
        val getResponse = get("/api/bookings/${createdBooking.id}")

        // Verify response
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val booking = getResponse.body<BookingResponse>()
        assertEquals(createdBooking.id, booking.id)
        assertEquals(customer.id, booking.customerId)
        assertEquals(bookingDate, booking.bookingDate)
        assertEquals("200.75", booking.amount)
    }

    @Test
    fun `update booking returns 200 OK`() = withApp {
        // Create a customer first
        val customer = createTestCustomer(this)

        // Create a booking
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val createResponse =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateBooking(
                        customerId = customer.id,
                        bookingDate = bookingDate,
                        amount = "300.25",
                    )
                )
            }
        val createdBooking = createResponse.body<BookingResponse>()

        // Update the booking
        val newBookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val updateResponse =
            put("/api/bookings/${createdBooking.id}") {
                contentType(ContentType.Application.Json)
                setBody(UpdateBooking(bookingDate = newBookingDate, amount = "350.75"))
            }

        // Verify response
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updatedBooking = updateResponse.body<BookingResponse>()
        assertEquals(createdBooking.id, updatedBooking.id)
        assertEquals(customer.id, updatedBooking.customerId)
        assertEquals(newBookingDate, updatedBooking.bookingDate)
        assertEquals("350.75", updatedBooking.amount)
    }

    @Test
    fun `delete booking returns 204 No Content`() = withApp {
        // Create a customer first
        val customer = createTestCustomer(this)

        // Create a booking
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val createResponse =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateBooking(
                        customerId = customer.id,
                        bookingDate = bookingDate,
                        amount = "400.00",
                    )
                )
            }
        val createdBooking = createResponse.body<BookingResponse>()

        // Delete the booking
        val deleteResponse = delete("/api/bookings/${createdBooking.id}")

        // Verify response
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        // Try to get the deleted booking
        val getResponse = get("/api/bookings/${createdBooking.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `get bookings for customer returns 200 OK`() = withApp {
        // Create a customer first
        val customer = createTestCustomer(this)

        // Create multiple bookings for the customer
        val bookingDate1 = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        post("/api/bookings") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateBooking(
                    customerId = customer.id,
                    bookingDate = bookingDate1,
                    amount = "500.50",
                )
            )
        }

        val bookingDate2 = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        post("/api/bookings") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateBooking(
                    customerId = customer.id,
                    bookingDate = bookingDate2,
                    amount = "600.75",
                )
            )
        }

        // Get all bookings for the customer
        val getResponse = get("/api/bookings/customer/${customer.id}")

        // Verify response
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val bookings = getResponse.body<List<BookingResponse>>()
        assertEquals(2, bookings.size)
        assertTrue(bookings.all { it.customerId == customer.id })
    }

    @Test
    fun `get non-existent booking returns 404 Not Found`() = withApp {
        val response = get("/api/bookings/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `update non-existent booking returns 404 Not Found`() = withApp {
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val response =
            put("/api/bookings/9999") {
                contentType(ContentType.Application.Json)
                setBody(UpdateBooking(bookingDate = bookingDate, amount = "999.99"))
            }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `delete non-existent booking returns 404 Not Found`() = withApp {
        val response = delete("/api/bookings/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `create booking with non-existent customer returns 400 Bad Request`() = withApp {
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val response =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateBooking(customerId = 9999, bookingDate = bookingDate, amount = "700.00")
                )
            }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
