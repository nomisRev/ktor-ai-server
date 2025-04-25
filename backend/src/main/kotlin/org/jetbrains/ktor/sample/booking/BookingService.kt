package org.jetbrains.ktor.sample.booking

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal

/**
 * Service class for Booking CRUD operations
 */
class BookingService(private val database: Database) {

    /**
     * Creates a new booking
     * @param customerId ID of the customer making the booking
     * @param bookingDate Date and time of the booking
     * @param amount Amount of the booking
     * @return The created booking with ID
     * @throws Exception if the customer doesn't exist or if there's an error creating the booking
     */
    suspend fun createBooking(customerId: Int, bookingDate: LocalDateTime, amount: BigDecimal): Booking = 
        newSuspendedTransaction(Dispatchers.IO, database) {
            try {
                // Check if customer exists
                val customerExists = Customers.selectAll()
                    .where { Customers.id eq customerId }
                    .count() > 0
                
                if (!customerExists) {
                    throw Exception("Customer with ID $customerId not found")
                }
                
                // Insert the booking and get the generated ID
                val id = Bookings.insertAndGetId {
                    it[Bookings.customerId] = customerId
                    it[Bookings.bookingDate] = bookingDate
                    it[Bookings.amount] = amount
                }
                
                Booking(id.value, customerId, bookingDate, amount)
            } catch (e: Exception) {
                throw Exception("Failed to create booking: ${e.message}", e)
            }
        }

    /**
     * Gets a booking by ID
     * @param id Booking ID
     * @return The booking if found, null otherwise
     */
    suspend fun getBookingById(id: Int): Booking? = newSuspendedTransaction(Dispatchers.IO, database) {
        try {
            Bookings.selectAll()
                .where { Bookings.id eq id }
                .mapNotNull { toBooking(it) }
                .singleOrNull()
        } catch (e: Exception) {
            throw Exception("Failed to get booking: ${e.message}", e)
        }
    }

    /**
     * Updates a booking
     * @param id Booking ID
     * @param bookingDate New booking date (optional)
     * @param amount New amount (optional)
     * @return The updated booking if found, null otherwise
     */
    suspend fun updateBooking(id: Int, bookingDate: LocalDateTime? = null, amount: BigDecimal? = null): Booking? = 
        newSuspendedTransaction(Dispatchers.IO, database) {
            try {
                // First check if booking exists
                val exists = Bookings.selectAll()
                    .where { Bookings.id eq id }
                    .count() > 0
                
                if (!exists) {
                    return@newSuspendedTransaction null
                }
                
                // Update only provided fields
                Bookings.update({ Bookings.id eq id }) {
                    bookingDate?.let { date -> it[Bookings.bookingDate] = date }
                    amount?.let { amt -> it[Bookings.amount] = amt }
                }
                
                // Return updated booking
                getBookingById(id)
            } catch (e: Exception) {
                throw Exception("Failed to update booking: ${e.message}", e)
            }
        }

    /**
     * Deletes a booking
     * @param id Booking ID
     * @return true if booking was deleted, false if not found
     */
    suspend fun deleteBooking(id: Int): Boolean = newSuspendedTransaction(Dispatchers.IO, database) {
        try {
            Bookings.deleteWhere { Bookings.id eq id } > 0
        } catch (e: Exception) {
            throw Exception("Failed to delete booking: ${e.message}", e)
        }
    }

    /**
     * Gets all bookings for a customer
     * @param customerId Customer ID
     * @return List of bookings for the customer
     */
    suspend fun getBookingsForCustomer(customerId: Int): List<Booking> = newSuspendedTransaction(Dispatchers.IO, database) {
        try {
            Bookings.selectAll()
                .where { Bookings.customerId eq customerId }
                .mapNotNull { toBooking(it) }
        } catch (e: Exception) {
            throw Exception("Failed to get bookings for customer: ${e.message}", e)
        }
    }

    /**
     * Maps a ResultRow to a Booking object
     */
    private fun toBooking(row: ResultRow): Booking =
        Booking(
            id = row[Bookings.id].value,
            customerId = row[Bookings.customerId].value,
            bookingDate = row[Bookings.bookingDate],
            amount = row[Bookings.amount]
        )
}