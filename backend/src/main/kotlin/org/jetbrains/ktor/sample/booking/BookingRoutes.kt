package org.jetbrains.ktor.sample.booking

import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Routing.installBookingRoutes(bookingRepository: BookingRepository) {
    authenticate {
        route("/api/bookings") {
            post {
                val request = call.receive<CreateBooking>()
                val booking =
                    bookingRepository.createBooking(
                        customerId = request.customerId,
                        amount = request.amount,
                    )
                call.respond(Created, booking)
            }

            get("/{id}") {
                val id =
                    call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(BadRequest, "Invalid booking ID")

                val booking = bookingRepository.getBookingById(id)
                if (booking != null) {
                    call.respond(OK, booking)
                } else {
                    call.respond(NotFound, "Booking not found")
                }
            }

            put("/{id}") {
                val id =
                    call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(BadRequest, "Invalid booking ID")
                val request = call.receive<UpdateBooking>()

                val updatedBooking =
                    bookingRepository.updateBooking(
                        id = id,
                        bookingDate = request.bookingDate,
                        amount = request.amount,
                    )

                if (updatedBooking != null) {
                    call.respond(OK, updatedBooking)
                } else {
                    call.respond(NotFound, "Booking not found")
                }
            }

            delete("/{id}") {
                val id =
                    call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(BadRequest, "Invalid booking ID")

                val deleted = bookingRepository.deleteBooking(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(NotFound, "Booking not found")
                }
            }

            get("/customer/{customerId}") {
                val customerId =
                    call.parameters["customerId"]?.toIntOrNull()
                        ?: return@get call.respond(BadRequest, "Invalid customer ID")

                val bookings = bookingRepository.getBookingsForCustomer(customerId)
                call.respond(OK, bookings)
            }
        }
    }
}
