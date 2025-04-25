package org.jetbrains.ktor.sample.booking

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.ktor.sample.DatabaseSpec
import org.junit.Test

class BookingServiceTest : DatabaseSpec() {
    private val customerRepository by lazy { CustomerRepository(database) }
    private val bookingRepository by lazy { BookingRepository(database) }

    /** Always generate random data for integration testing so tests can run in parallel */
    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createTestCustomer(
        name: String = "Test Customer ${Uuid.random()}",
        email: String = "test.customer.${Uuid.random()}@example.com",
    ): Customer = customerRepository.createCustomer(name, email)

    @Test
    fun `test create and get booking`() = runBlocking {
        val customer = createTestCustomer()
        val createdBooking = bookingRepository.createBooking(customer.id, 100.50)

        val retrievedBooking = bookingRepository.getBookingById(createdBooking.id)

        assertEquals(retrievedBooking, createdBooking)
    }

    @Test
    fun `test update booking`() = runBlocking {
        val customer = createTestCustomer()
        val createdBooking = bookingRepository.createBooking(customer.id, 200.75)

        val newBookingDate = Clock.System.now()
        val newAmount = 250.25

        val updatedBooking =
            bookingRepository.updateBooking(createdBooking.id, newBookingDate, amount = newAmount)

        assertEquals(
            updatedBooking,
            createdBooking.copy(bookingDate = newBookingDate, amount = newAmount),
        )
    }

    @Test
    fun `test partial update booking`() = runBlocking {
        val customer = createTestCustomer()
        val amount = 300.25
        val createdBooking = bookingRepository.createBooking(customer.id, amount)
        val newAmount = 350.75

        val updatedBooking =
            bookingRepository.updateBooking(
                createdBooking.id,
                bookingDate = null,
                amount = newAmount,
            )

        assertEquals(updatedBooking, createdBooking.copy(amount = newAmount))
    }

    @Test
    fun `test delete booking`() = runBlocking {
        val customer = createTestCustomer()
        val amount = 400.00
        val createdBooking = bookingRepository.createBooking(customer.id, amount)

        val deleted = bookingRepository.deleteBooking(createdBooking.id)
        assertTrue(deleted)

        val retrievedBooking = bookingRepository.getBookingById(createdBooking.id)
        assertNull(retrievedBooking)
    }

    @Test
    fun `test get bookings for customer`() = runBlocking {
        val customer = createTestCustomer()
        val amount1 = 500.50
        val booking1 = bookingRepository.createBooking(customer.id, amount1)

        val amount2 = 600.75
        val booking2 = bookingRepository.createBooking(customer.id, amount2)

        val customerBookings = bookingRepository.getBookingsForCustomer(customer.id)

        assertEquals(listOf(booking1, booking2), customerBookings)
    }

    @Test
    fun `test create booking for non-existent customer`() =
        runBlocking<Unit> {
            val amount = 700.00
            bookingRepository.createBooking(999, amount)
        }

    @Test
    fun `test get non-existent booking`() = runBlocking {
        val retrievedBooking = bookingRepository.getBookingById(999)
        assertNull(retrievedBooking)
    }

    @Test
    fun `test update non-existent booking`() = runBlocking {
        val amount = 800.00

        val updatedBooking = bookingRepository.updateBooking(999, amount = amount)

        assertNull(updatedBooking)
    }

    @Test
    fun `test delete non-existent booking`() = runBlocking {
        val deleted = bookingRepository.deleteBooking(999)

        assertEquals(false, deleted)
    }

    @Test
    fun `test get bookings for non-existent customer`() = runBlocking {
        val customerBookings = bookingRepository.getBookingsForCustomer(999)

        assertEquals(0, customerBookings.size)
    }
}
