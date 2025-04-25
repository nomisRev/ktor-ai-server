package org.jetbrains.ktor.sample.booking

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Service class for Customer CRUD operations
 */
class CustomerService(private val database: Database) {

    /**
     * Creates a new customer
     * @param name Customer name
     * @param email Customer email (must be unique)
     * @return The created customer with ID
     * @throws Exception if a customer with the same email already exists
     */
    suspend fun createCustomer(name: String, email: String): Customer = newSuspendedTransaction(Dispatchers.IO, database) {
        try {
            val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            // Insert the customer and get the generated ID
            val id = Customers.insertAndGetId {
                it[Customers.name] = name
                it[Customers.email] = email
                it[Customers.createdAt] = currentTime
            }

            Customer(id.value, name, email, currentTime)
        } catch (e: Exception) {
            throw Exception("Failed to create customer: ${e.message}", e)
        }
    }

    /**
     * Gets a customer by ID
     * @param id Customer ID
     * @return The customer if found, null otherwise
     */
    suspend fun getCustomerById(id: Int): Customer? = newSuspendedTransaction(Dispatchers.IO, database) {
        try {
            Customers.selectAll()
                .where { Customers.id eq id }
                .mapNotNull { toCustomer(it) }
                .singleOrNull()
        } catch (e: Exception) {
            throw Exception("Failed to get customer: ${e.message}", e)
        }
    }

    /**
     * Updates a customer
     * @param id Customer ID
     * @param name New name (optional)
     * @param email New email (optional)
     * @return The updated customer if found, null otherwise
     * @throws Exception if a customer with the new email already exists
     */
    suspend fun updateCustomer(id: Int, name: String? = null, email: String? = null): Customer? = 
        newSuspendedTransaction(Dispatchers.IO, database) {
            try {
                // First check if customer exists
                val exists = Customers.selectAll()
                    .where { Customers.id eq id }
                    .count() > 0

                if (!exists) {
                    return@newSuspendedTransaction null
                }

                // Update only provided fields
                Customers.update({ Customers.id eq id }) {
                    name?.let { n -> it[Customers.name] = n }
                    email?.let { e -> it[Customers.email] = e }
                }

                // Return updated customer
                getCustomerById(id)
            } catch (e: Exception) {
                throw Exception("Failed to update customer: ${e.message}", e)
            }
        }

    /**
     * Deletes a customer
     * @param id Customer ID
     * @return true if customer was deleted, false if not found
     */
    suspend fun deleteCustomer(id: Int): Boolean = newSuspendedTransaction(Dispatchers.IO, database) {
        try {
            Customers.deleteWhere { Customers.id eq id } > 0
        } catch (e: Exception) {
            throw Exception("Failed to delete customer: ${e.message}", e)
        }
    }

    /**
     * Maps a ResultRow to a Customer object
     */
    private fun toCustomer(row: ResultRow): Customer =
        Customer(
            id = row[Customers.id].value,
            name = row[Customers.name],
            email = row[Customers.email],
            createdAt = row[Customers.createdAt]
        )
}
