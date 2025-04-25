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

/** Installs routes for customer operations */
fun Routing.installCustomerRoutes(customerService: CustomerService) {
    authenticate {
        route("/api/customers") {
            // Create a new customer
            post {
                try {
                    val request = call.receive<CreateCustomer>()
                    val customer =
                        customerService.createCustomer(name = request.name, email = request.email)
                    call.respond(HttpStatusCode.Created, customer)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Unknown error")),
                    )
                }
            }

            // Get customer by ID
            get("/{id}") {
                try {
                    val id =
                        call.parameters["id"]?.toIntOrNull()
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid customer ID"),
                            )

                    val customer = customerService.getCustomerById(id)
                    if (customer != null) {
                        call.respond(HttpStatusCode.OK, customer)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "Customer not found"),
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error")),
                    )
                }
            }

            // Update customer
            put("/{id}") {
                try {
                    val id =
                        call.parameters["id"]?.toIntOrNull()
                            ?: return@put call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid customer ID"),
                            )

                    val request = call.receive<UpdateCustomer>()
                    val updatedCustomer =
                        customerService.updateCustomer(
                            id = id,
                            name = request.name,
                            email = request.email,
                        )

                    if (updatedCustomer != null) {
                        call.respond(HttpStatusCode.OK, updatedCustomer)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "Customer not found"),
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Unknown error")),
                    )
                }
            }

            // Delete customer
            delete("/{id}") {
                try {
                    val id =
                        call.parameters["id"]?.toIntOrNull()
                            ?: return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid customer ID"),
                            )

                    val deleted = customerService.deleteCustomer(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "Customer not found"),
                        )
                    }
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
