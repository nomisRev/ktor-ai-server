package org.jetbrains.ktor.sample.booking

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Request model for creating a customer
 */
@Serializable
data class CreateCustomerRequest(
    val name: String,
    val email: String
)

/**
 * Request model for updating a customer
 */
@Serializable
data class UpdateCustomerRequest(
    val name: String? = null,
    val email: String? = null
)

/**
 * Response model for customer operations
 */
@Serializable
data class CustomerResponse(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime
)

/**
 * Request model for creating a booking
 */
@Serializable
data class CreateBookingRequest(
    val customerId: Int,
    val bookingDate: LocalDateTime,
    val amount: String // Using String for BigDecimal as it's not directly serializable
)

/**
 * Request model for updating a booking
 */
@Serializable
data class UpdateBookingRequest(
    val bookingDate: LocalDateTime? = null,
    val amount: String? = null // Using String for BigDecimal as it's not directly serializable
)

/**
 * Response model for booking operations
 */
@Serializable
data class BookingResponse(
    val id: Int,
    val customerId: Int,
    val bookingDate: LocalDateTime,
    val amount: String // Using String for BigDecimal as it's not directly serializable
)

/**
 * Extension function to convert Customer to CustomerResponse
 */
fun Customer.toResponse(): CustomerResponse = CustomerResponse(
    id = id ?: throw IllegalStateException("Customer ID cannot be null"),
    name = name,
    email = email,
    createdAt = createdAt ?: throw IllegalStateException("Customer createdAt cannot be null")
)

/**
 * Extension function to convert Booking to BookingResponse
 */
fun Booking.toResponse(): BookingResponse = BookingResponse(
    id = id ?: throw IllegalStateException("Booking ID cannot be null"),
    customerId = customerId,
    bookingDate = bookingDate,
    amount = amount.toPlainString()
)