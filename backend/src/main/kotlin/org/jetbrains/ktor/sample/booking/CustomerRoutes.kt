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

fun Routing.installCustomerRoutes(customerRepository: CustomerRepository) {
    authenticate {
        route("/api/customers") {
            post {
                val request = call.receive<CreateCustomer>()
                val customer =
                    customerRepository.createCustomer(name = request.name, email = request.email)
                call.respond(Created, customer)
            }

            get("/{id}") {
                val id =
                    call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(BadRequest, "Invalid customer ID")

                val customer = customerRepository.getCustomerById(id)
                if (customer != null) {
                    call.respond(OK, customer)
                } else {
                    call.respond(NotFound, "Customer not found")
                }
            }
        }

        put("/{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(BadRequest, "Invalid customer ID")
            val request = call.receive<UpdateCustomer>()

            val updatedCustomer =
                customerRepository.updateCustomer(
                    id = id,
                    name = request.name,
                    email = request.email,
                )

            if (updatedCustomer != null) {
                call.respond(OK, updatedCustomer)
            } else {
                call.respond(NotFound, "Customer not found")
            }
        }

        delete("/{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(BadRequest, "Invalid customer ID")

            val deleted = customerRepository.deleteCustomer(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(NotFound, "Customer not found")
            }
        }
    }
}
