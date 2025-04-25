package org.jetbrains.ktor.sample.booking

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import kotlinx.datetime.LocalDateTime

/**
 * Exposed table definition for Customer
 */
object Customers : IntIdTable("customers") {
    val name: Column<String> = varchar("name", 255)
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val createdAt: Column<LocalDateTime> = datetime("created_at")
}

/**
 * Exposed table definition for Booking
 */
object Bookings : IntIdTable("bookings") {
    val customerId = reference("customer_id", Customers)
    val bookingDate: Column<LocalDateTime> = datetime("booking_date")
    val amount = decimal("amount", 10, 2)
}
