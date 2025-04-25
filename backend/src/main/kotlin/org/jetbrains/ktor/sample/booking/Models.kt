package org.jetbrains.ktor.sample.booking

import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

/**
 * Data class representing a Customer
 */
data class Customer(
    val id: Int? = null,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime? = null
)

/**
 * Data class representing a Booking
 */
data class Booking(
    val id: Int? = null,
    val customerId: Int,
    val bookingDate: LocalDateTime,
    val amount: BigDecimal
)