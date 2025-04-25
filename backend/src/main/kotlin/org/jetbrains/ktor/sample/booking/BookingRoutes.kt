package org.jetbrains.ktor.sample.booking

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.coroutines.Deferred
import java.math.BigDecimal

/**
 * Installs routes for booking operations
 */
fun Routing.installBookingRoutes(bookingService: Deferred<BookingService>) {
    authenticate {
        route("/api/bookings") {
            // Create a new booking
            post {
                try {
                    val request = call.receive<CreateBookingRequest>()
                    val booking = bookingService.await().createBooking(
                        customerId = request.customerId,
                        bookingDate = request.bookingDate,
                        amount = BigDecimal(request.amount)
                    )
                    call.respond(HttpStatusCode.Created, booking.toResponse())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }

            // Get booking by ID
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid booking ID"))

                    val booking = bookingService.await().getBookingById(id)
                    if (booking != null) {
                        call.respond(HttpStatusCode.OK, booking.toResponse())
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Booking not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }

            // Update booking
            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid booking ID"))

                    val request = call.receive<UpdateBookingRequest>()
                    val amount = request.amount?.let { BigDecimal(it) }
                    
                    val updatedBooking = bookingService.await().updateBooking(
                        id = id,
                        bookingDate = request.bookingDate,
                        amount = amount
                    )

                    if (updatedBooking != null) {
                        call.respond(HttpStatusCode.OK, updatedBooking.toResponse())
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Booking not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }

            // Delete booking
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid booking ID"))

                    val deleted = bookingService.await().deleteBooking(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Booking not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }

            // Get bookings for a customer
            get("/customer/{customerId}") {
                try {
                    val customerId = call.parameters["customerId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid customer ID"))

                    val bookings = bookingService.await().getBookingsForCustomer(customerId)
                    call.respond(HttpStatusCode.OK, bookings.map { it.toResponse() })
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
        }
    }
}