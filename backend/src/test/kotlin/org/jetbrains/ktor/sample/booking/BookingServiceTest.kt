package org.jetbrains.ktor.sample.booking

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.ktor.sample.DatabaseSpec
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookingServiceTest : DatabaseSpec() {
    private val customerService by lazy { CustomerService(database) }
    private val bookingService by lazy { BookingService(database) }
    
    // Helper function to create a test customer
    private suspend fun createTestCustomer(name: String = "Test Customer", email: String = "test.customer@example.com"): Customer {
        return customerService.createCustomer(name, email)
    }
    
    @Test
    fun `test create and get booking`() = runBlocking {
        // Create a customer first
        val customer = createTestCustomer()
        
        // Create a booking
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val amount = BigDecimal("100.50")
        
        val createdBooking = bookingService.createBooking(customer.id!!, bookingDate, amount)
        
        // Verify the created booking
        assertNotNull(createdBooking.id)
        assertEquals(customer.id, createdBooking.customerId)
        assertEquals(bookingDate, createdBooking.bookingDate)
        assertEquals(amount, createdBooking.amount)
        
        // Get the booking by ID
        val retrievedBooking = bookingService.getBookingById(createdBooking.id!!)
        
        // Verify the retrieved booking
        assertNotNull(retrievedBooking)
        assertEquals(createdBooking.id, retrievedBooking?.id)
        assertEquals(customer.id, retrievedBooking?.customerId)
        assertEquals(bookingDate, retrievedBooking?.bookingDate)
        assertEquals(amount, retrievedBooking?.amount)
    }
    
    @Test
    fun `test update booking`() = runBlocking {
        // Create a customer first
        val customer = createTestCustomer()
        
        // Create a booking
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val amount = BigDecimal("200.75")
        
        val createdBooking = bookingService.createBooking(customer.id!!, bookingDate, amount)
        
        // Update the booking
        val newBookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val newAmount = BigDecimal("250.25")
        
        val updatedBooking = bookingService.updateBooking(
            createdBooking.id!!,
            bookingDate = newBookingDate,
            amount = newAmount
        )
        
        // Verify the updated booking
        assertNotNull(updatedBooking)
        assertEquals(createdBooking.id, updatedBooking?.id)
        assertEquals(customer.id, updatedBooking?.customerId)
        assertEquals(newBookingDate, updatedBooking?.bookingDate)
        assertEquals(newAmount, updatedBooking?.amount)
    }
    
    @Test
    fun `test partial update booking`() = runBlocking {
        // Create a customer first
        val customer = createTestCustomer()
        
        // Create a booking
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val amount = BigDecimal("300.25")
        
        val createdBooking = bookingService.createBooking(customer.id!!, bookingDate, amount)
        
        // Update only the amount
        val newAmount = BigDecimal("350.75")
        
        val updatedBooking = bookingService.updateBooking(
            createdBooking.id!!,
            bookingDate = null,
            amount = newAmount
        )
        
        // Verify the updated booking
        assertNotNull(updatedBooking)
        assertEquals(createdBooking.id, updatedBooking?.id)
        assertEquals(customer.id, updatedBooking?.customerId)
        assertEquals(bookingDate, updatedBooking?.bookingDate) // Booking date should remain unchanged
        assertEquals(newAmount, updatedBooking?.amount)
    }
    
    @Test
    fun `test delete booking`() = runBlocking {
        // Create a customer first
        val customer = createTestCustomer()
        
        // Create a booking
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val amount = BigDecimal("400.00")
        
        val createdBooking = bookingService.createBooking(customer.id!!, bookingDate, amount)
        
        // Delete the booking
        val deleted = bookingService.deleteBooking(createdBooking.id!!)
        
        // Verify the booking was deleted
        assertTrue(deleted)
        
        // Try to get the deleted booking
        val retrievedBooking = bookingService.getBookingById(createdBooking.id!!)
        
        // Verify the booking is no longer found
        assertNull(retrievedBooking)
    }
    
    @Test
    fun `test get bookings for customer`() = runBlocking {
        // Create a customer
        val customer = createTestCustomer()
        
        // Create multiple bookings for the customer
        val bookingDate1 = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val amount1 = BigDecimal("500.50")
        val booking1 = bookingService.createBooking(customer.id!!, bookingDate1, amount1)
        
        val bookingDate2 = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val amount2 = BigDecimal("600.75")
        val booking2 = bookingService.createBooking(customer.id!!, bookingDate2, amount2)
        
        // Get all bookings for the customer
        val customerBookings = bookingService.getBookingsForCustomer(customer.id!!)
        
        // Verify the bookings
        assertEquals(2, customerBookings.size)
        assertTrue(customerBookings.any { it.id == booking1.id })
        assertTrue(customerBookings.any { it.id == booking2.id })
    }
    
    @Test
    fun `test create booking for non-existent customer`() = runBlocking {
        // Try to create a booking for a non-existent customer
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val amount = BigDecimal("700.00")
        
        try {
            bookingService.createBooking(999, bookingDate, amount)
            // If we get here, the test should fail
            assertTrue(false, "Expected an exception when creating a booking for a non-existent customer")
        } catch (e: Exception) {
            // Expected exception
            assertTrue(e.message?.contains("Customer with ID 999 not found") ?: false)
        }
    }
    
    @Test
    fun `test get non-existent booking`() = runBlocking {
        // Try to get a booking with a non-existent ID
        val retrievedBooking = bookingService.getBookingById(999)
        
        // Verify the booking is not found
        assertNull(retrievedBooking)
    }
    
    @Test
    fun `test update non-existent booking`() = runBlocking {
        // Try to update a booking with a non-existent ID
        val bookingDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val amount = BigDecimal("800.00")
        
        val updatedBooking = bookingService.updateBooking(
            999,
            bookingDate = bookingDate,
            amount = amount
        )
        
        // Verify the update returns null
        assertNull(updatedBooking)
    }
    
    @Test
    fun `test delete non-existent booking`() = runBlocking {
        // Try to delete a booking with a non-existent ID
        val deleted = bookingService.deleteBooking(999)
        
        // Verify the delete returns false
        assertEquals(false, deleted)
    }
    
    @Test
    fun `test get bookings for non-existent customer`() = runBlocking {
        // Try to get bookings for a non-existent customer
        val customerBookings = bookingService.getBookingsForCustomer(999)
        
        // Verify an empty list is returned
        assertEquals(0, customerBookings.size)
    }
}