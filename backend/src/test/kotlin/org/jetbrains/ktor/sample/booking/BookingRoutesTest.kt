package org.jetbrains.ktor.sample.booking

import io.ktor.client.HttpClient
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import org.jetbrains.ktor.sample.withApp

class BookingRoutesTest {

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun HttpClient.createTestCustomer(): Customer {
        val response =
            post("/api/customers") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateCustomer(
                        name = "Booking Test Customer ${Uuid.random()}",
                        email = "booking.test.customer.${Uuid.random()}@example.com",
                    )
                )
            }
        return response.body()
    }

    @Test
    fun `create booking returns 201 Created`() = withApp {
        val customer = createTestCustomer()

        val createResponse =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(CreateBooking(customerId = customer.id, amount = 100.50))
            }

        assertEquals(HttpStatusCode.Created, createResponse.status)
        val booking = createResponse.body<Booking>()
        assertNotNull(booking.id)
        assertEquals(customer.id, booking.customerId)
        assertEquals(100.50, booking.amount)
    }

    @Test
    fun `get booking returns 200 OK`() = withApp {
        val customer = createTestCustomer()

        val createdBooking =
            post("/api/bookings") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateBooking(customerId = customer.id, amount = 200.75))
                }
                .body<Booking>()

        val getResponse = get("/api/bookings/${createdBooking.id}")

        assertEquals(HttpStatusCode.OK, getResponse.status)

        val booking = getResponse.body<Booking>()
        assertEquals(createdBooking.id, booking.id)
        assertEquals(customer.id, booking.customerId)
        assertEquals(200.75, booking.amount)
    }

    @Test
    fun `update booking returns 200 OK`() = withApp {
        val customer = createTestCustomer()

        val createResponse =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(CreateBooking(customerId = customer.id, amount = 300.25))
            }
        val createdBooking = createResponse.body<Booking>()

        val newBookingDate = Clock.System.now()
        val updateResponse =
            put("/api/bookings/${createdBooking.id}") {
                contentType(ContentType.Application.Json)
                setBody(UpdateBooking(bookingDate = newBookingDate, amount = 350.75))
            }

        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updatedBooking = updateResponse.body<Booking>()
        assertEquals(createdBooking.id, updatedBooking.id)
        assertEquals(customer.id, updatedBooking.customerId)
        assertEquals(newBookingDate, updatedBooking.bookingDate)
        assertEquals(350.75, updatedBooking.amount)
    }

    @Test
    fun `delete booking returns 204 No Content`() = withApp {
        val customer = this.createTestCustomer()

        val createResponse =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(CreateBooking(customerId = customer.id, amount = 400.00))
            }
        val createdBooking = createResponse.body<Booking>()

        val deleteResponse = delete("/api/bookings/${createdBooking.id}")

        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val getResponse = get("/api/bookings/${createdBooking.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `get bookings for customer returns 200 OK`() = withApp {
        val customer = this.createTestCustomer()

        val booking1 =
            post("/api/bookings") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateBooking(customerId = customer.id, amount = 500.50))
                }
                .body<Booking>()

        val booking2 =
            post("/api/bookings") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateBooking(customerId = customer.id, amount = 600.75))
                }
                .body<Booking>()

        val getResponse = get("/api/bookings/customer/${customer.id}")

        assertEquals(HttpStatusCode.OK, getResponse.status)
        val bookings = getResponse.body<List<Booking>>()
        assertEquals(listOf(booking1, booking2), bookings)
    }

    @Test
    fun `get non-existent booking returns 404 Not Found`() = withApp {
        val response = get("/api/bookings/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `update non-existent booking returns 404 Not Found`() = withApp {
        val bookingDate = Clock.System.now()
        val response =
            put("/api/bookings/9999") {
                contentType(ContentType.Application.Json)
                setBody(UpdateBooking(bookingDate = bookingDate, amount = 999.99))
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
        val response =
            post("/api/bookings") {
                contentType(ContentType.Application.Json)
                setBody(CreateBooking(customerId = 9999, amount = 700.00))
            }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
