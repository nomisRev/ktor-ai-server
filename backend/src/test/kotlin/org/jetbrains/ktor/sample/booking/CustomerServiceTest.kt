package org.jetbrains.ktor.sample.booking

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.jetbrains.ktor.sample.DatabaseSpec
import org.junit.Test

class CustomerServiceTest : DatabaseSpec() {
    private val customerRepository by lazy { CustomerRepository(database) }

    @Test
    fun `test create and get customer`() = runBlocking {
        val name = "John Doe"
        val email = "john.doe@example.com"

        val createdCustomer = customerRepository.createCustomer(name, email)
        val retrievedCustomer = customerRepository.getCustomerById(createdCustomer.id)

        assertEquals(retrievedCustomer, createdCustomer)
    }

    @Test
    fun `test update customer`() = runBlocking {
        val name = "Jane Doe"
        val email = "jane.doe@example.com"

        val createdCustomer = customerRepository.createCustomer(name, email)

        val newName = "Jane Smith"
        val newEmail = "jane.smith@example.com"

        val updatedCustomer =
            customerRepository.updateCustomer(createdCustomer.id, name = newName, email = newEmail)

        assertEquals(updatedCustomer, createdCustomer.copy(name = newName, email = newEmail))
    }

    @Test
    fun `test partial update customer`() = runBlocking {
        val name = "Bob Johnson"
        val email = "bob.johnson@example.com"

        val createdCustomer = customerRepository.createCustomer(name, email)

        val newName = "Robert Johnson"

        val updatedCustomer =
            customerRepository.updateCustomer(createdCustomer.id, name = newName, email = null)

        assertEquals(updatedCustomer, createdCustomer.copy(name = newName))
    }

    @Test
    fun `test delete customer`() = runBlocking {
        val name = "Alice Brown"
        val email = "alice.brown@example.com"
        val createdCustomer = customerRepository.createCustomer(name, email)

        val deleted = customerRepository.deleteCustomer(createdCustomer.id)
        assertTrue(deleted)

        val retrievedCustomer = customerRepository.getCustomerById(createdCustomer.id)
        assertNull(retrievedCustomer)
    }

    @Test
    fun `test get non-existent customer`() = runBlocking {
        val retrievedCustomer = customerRepository.getCustomerById(999)

        assertNull(retrievedCustomer)
    }

    @Test
    fun `test update non-existent customer`() = runBlocking {
        val updatedCustomer =
            customerRepository.updateCustomer(
                999,
                name = "Non-existent Customer",
                email = "non.existent@example.com",
            )

        assertNull(updatedCustomer)
    }

    @Test
    fun `test delete non-existent customer`() = runBlocking {
        val deleted = customerRepository.deleteCustomer(999)

        assertEquals(false, deleted)
    }
}
