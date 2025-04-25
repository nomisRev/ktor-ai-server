package org.jetbrains.ktor.sample.booking

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class BookingService(private val database: Database) {
    suspend fun createBooking(customerId: Int, amount: Double): Booking =
        withContext(Dispatchers.IO) {
            transaction(database) {
                val inserted =
                    Bookings.insertReturning(listOf(Bookings.id, Bookings.bookingDate)) {
                            it[Bookings.customerId] = customerId
                            it[Bookings.bookingDate] = bookingDate
                            it[Bookings.amount] = amount
                        }
                        .single()

                Booking(
                    inserted[Bookings.id].value,
                    customerId,
                    inserted[Bookings.bookingDate],
                    amount,
                )
            }
        }

    suspend fun getBookingById(id: Int): Booking? =
        withContext(Dispatchers.IO) {
            transaction(database) {
                Bookings.selectAll().where { Bookings.id eq id }.singleOrNull()?.toBooking()
            }
        }

    suspend fun updateBooking(
        id: Int,
        bookingDate: Instant? = null,
        amount: Double? = null,
    ): Booking? =
        withContext(Dispatchers.IO) {
            transaction(database) {
                Bookings.updateReturning(where = { Bookings.id eq id }) {
                        if (bookingDate != null) it[Bookings.bookingDate] = bookingDate
                        if (amount != null) it[Bookings.amount] = amount
                    }
                    .singleOrNull()
                    ?.toBooking()
            }
        }

    suspend fun deleteBooking(id: Int): Boolean =
        withContext(Dispatchers.IO) {
            transaction(database) { Bookings.deleteWhere { Bookings.id eq id } > 0 }
        }

    suspend fun getBookingsForCustomer(customerId: Int): List<Booking> =
        withContext(Dispatchers.IO) {
            transaction(database) {
                Bookings.selectAll()
                    .where { Bookings.customerId eq customerId }
                    .mapNotNull { it.toBooking() }
            }
        }

    private fun ResultRow.toBooking(): Booking =
        Booking(
            id = this[Bookings.id].value,
            customerId = this[Bookings.customerId].value,
            bookingDate = this[Bookings.bookingDate],
            amount = this[Bookings.amount],
        )
}
