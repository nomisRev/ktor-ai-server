package org.jetbrains.ktor.sample.booking

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.ktor.sample.DatabaseSpec
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CustomerServiceTest : DatabaseSpec() {
    private val customerService by lazy { CustomerService(database) }
    
    @Test
    fun `test create and get customer`() = runBlocking {
        // Create a customer
        val name = "John Doe"
        val email = "john.doe@example.com"
        
        val createdCustomer = customerService.createCustomer(name, email)
        
        // Verify the created customer
        assertNotNull(createdCustomer.id)
        assertEquals(name, createdCustomer.name)
        assertEquals(email, createdCustomer.email)
        assertNotNull(createdCustomer.createdAt)
        
        // Get the customer by ID
        val retrievedCustomer = customerService.getCustomerById(createdCustomer.id!!)
        
        // Verify the retrieved customer
        assertNotNull(retrievedCustomer)
        assertEquals(createdCustomer.id, retrievedCustomer?.id)
        assertEquals(name, retrievedCustomer?.name)
        assertEquals(email, retrievedCustomer?.email)
        assertEquals(createdCustomer.createdAt, retrievedCustomer?.createdAt)
    }
    
    @Test
    fun `test update customer`() = runBlocking {
        // Create a customer
        val name = "Jane Doe"
        val email = "jane.doe@example.com"
        
        val createdCustomer = customerService.createCustomer(name, email)
        
        // Update the customer
        val newName = "Jane Smith"
        val newEmail = "jane.smith@example.com"
        
        val updatedCustomer = customerService.updateCustomer(
            createdCustomer.id!!,
            name = newName,
            email = newEmail
        )
        
        // Verify the updated customer
        assertNotNull(updatedCustomer)
        assertEquals(createdCustomer.id, updatedCustomer?.id)
        assertEquals(newName, updatedCustomer?.name)
        assertEquals(newEmail, updatedCustomer?.email)
        assertEquals(createdCustomer.createdAt, updatedCustomer?.createdAt)
    }
    
    @Test
    fun `test partial update customer`() = runBlocking {
        // Create a customer
        val name = "Bob Johnson"
        val email = "bob.johnson@example.com"
        
        val createdCustomer = customerService.createCustomer(name, email)
        
        // Update only the name
        val newName = "Robert Johnson"
        
        val updatedCustomer = customerService.updateCustomer(
            createdCustomer.id!!,
            name = newName,
            email = null
        )
        
        // Verify the updated customer
        assertNotNull(updatedCustomer)
        assertEquals(createdCustomer.id, updatedCustomer?.id)
        assertEquals(newName, updatedCustomer?.name)
        assertEquals(email, updatedCustomer?.email) // Email should remain unchanged
        assertEquals(createdCustomer.createdAt, updatedCustomer?.createdAt)
    }
    
    @Test
    fun `test delete customer`() = runBlocking {
        // Create a customer
        val name = "Alice Brown"
        val email = "alice.brown@example.com"
        
        val createdCustomer = customerService.createCustomer(name, email)
        
        // Delete the customer
        val deleted = customerService.deleteCustomer(createdCustomer.id!!)
        
        // Verify the customer was deleted
        assertTrue(deleted)
        
        // Try to get the deleted customer
        val retrievedCustomer = customerService.getCustomerById(createdCustomer.id!!)
        
        // Verify the customer is no longer found
        assertNull(retrievedCustomer)
    }
    
    @Test
    fun `test get non-existent customer`() = runBlocking {
        // Try to get a customer with a non-existent ID
        val retrievedCustomer = customerService.getCustomerById(999)
        
        // Verify the customer is not found
        assertNull(retrievedCustomer)
    }
    
    @Test
    fun `test update non-existent customer`() = runBlocking {
        // Try to update a customer with a non-existent ID
        val updatedCustomer = customerService.updateCustomer(
            999,
            name = "Non-existent Customer",
            email = "non.existent@example.com"
        )
        
        // Verify the update returns null
        assertNull(updatedCustomer)
    }
    
    @Test
    fun `test delete non-existent customer`() = runBlocking {
        // Try to delete a customer with a non-existent ID
        val deleted = customerService.deleteCustomer(999)
        
        // Verify the delete returns false
        assertEquals(false, deleted)
    }
}