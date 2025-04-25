package org.jetbrains.ktor.sample.booking

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable data class CreateCustomer(val name: String, val email: String)

@Serializable data class UpdateCustomer(val name: String? = null, val email: String? = null)

@Serializable data class CreateBooking(val customerId: Int, val amount: Double)

@Serializable
data class UpdateBooking(val bookingDate: Instant? = null, val amount: Double? = null)

data class Customer(val id: Int, val name: String, val email: String, val createdAt: Instant)

data class Booking(val id: Int, val customerId: Int, val bookingDate: Instant, val amount: Double)
