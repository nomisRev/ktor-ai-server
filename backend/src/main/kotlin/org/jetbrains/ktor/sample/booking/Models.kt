package org.jetbrains.ktor.sample.booking

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

@Serializable data class CreateCustomer(val name: String, val email: String)

@Serializable data class UpdateCustomer(val name: String? = null, val email: String? = null)

@Serializable
data class CreateBooking(val customerId: Int, val bookingDate: Instant, val amount: Double)

@Serializable
data class UpdateBooking(val bookingDate: Instant? = null, val amount: Double? = null)

data class Customer(val id: Int, val name: String, val email: String, val createdAt: Instant)

data class Booking(val id: Int, val customerId: Int, val bookingDate: Instant, val amount: Double)

object Customers : IntIdTable("customers", "customer_id") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object Bookings : IntIdTable("bookings") {
    val customerId = reference("customer_id", Customers)
    val bookingDate = timestamp("booking_date").defaultExpression(CurrentTimestamp)
    val amount = double("amount")
}
