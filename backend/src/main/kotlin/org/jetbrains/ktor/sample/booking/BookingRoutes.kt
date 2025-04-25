package org.jetbrains.ktor.sample.booking

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/** Installs routes for booking operations */
fun Routing.installBookingRoutes(bookingService: BookingService) {
    authenticate {
        route("/api/bookings") {
            post {
                try {
                    val request = call.receive<CreateBooking>()
                    val booking =
                        bookingService.createBooking(
                            customerId = request.customerId,
                            amount = request.amount,
                        )
                    call.respond(HttpStatusCode.Created, booking)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Unknown error")),
                    )
                }
            }

            get("/{id}") {
                try {
                    val id =
                        call.parameters["id"]?.toIntOrNull()
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid booking ID"),
                            )

                    val booking = bookingService.getBookingById(id)
                    if (booking != null) {
                        call.respond(HttpStatusCode.OK, booking)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Booking not found"))
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error")),
                    )
                }
            }

            put("/{id}") {
                try {
                    val id =
                        call.parameters["id"]?.toIntOrNull()
                            ?: return@put call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid booking ID"),
                            )

                    val request = call.receive<UpdateBooking>()

                    val updatedBooking =
                        bookingService.updateBooking(
                            id = id,
                            bookingDate = request.bookingDate,
                            amount = request.amount,
                        )

                    if (updatedBooking != null) {
                        call.respond(HttpStatusCode.OK, updatedBooking)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Booking not found"))
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Unknown error")),
                    )
                }
            }

            delete("/{id}") {
                try {
                    val id =
                        call.parameters["id"]?.toIntOrNull()
                            ?: return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid booking ID"),
                            )

                    val deleted = bookingService.deleteBooking(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Booking not found"))
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error")),
                    )
                }
            }

            get("/customer/{customerId}") {
                try {
                    val customerId =
                        call.parameters["customerId"]?.toIntOrNull()
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid customer ID"),
                            )

                    val bookings = bookingService.getBookingsForCustomer(customerId)
                    call.respond(HttpStatusCode.OK, bookings)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error")),
                    )
                }
            }
        }
    }
}
