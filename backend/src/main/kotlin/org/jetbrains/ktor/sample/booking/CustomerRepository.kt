package org.jetbrains.ktor.sample.booking

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

object Customers : IntIdTable("customers", "customer_id") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

class CustomerRepository(private val database: Database) {
    suspend fun createCustomer(name: String, email: String): Customer =
        withContext(Dispatchers.IO) {
            transaction(database) {
                val insert =
                    Customers.insertReturning(listOf(Customers.id, Customers.createdAt)) {
                            it[Customers.name] = name
                            it[Customers.email] = email
                        }
                        .single()

                Customer(insert[Customers.id].value, name, email, insert[Customers.createdAt])
            }
        }

    suspend fun getCustomerById(id: Int): Customer? =
        withContext(Dispatchers.IO) {
            transaction(database) {
                Customers.selectAll().where { Customers.id eq id }.singleOrNull()?.toCustomer()
            }
        }

    suspend fun updateCustomer(id: Int, name: String? = null, email: String? = null): Customer? =
        withContext(Dispatchers.IO) {
            transaction(database) {
                Customers.updateReturning(where = { Customers.id eq id }) {
                        if (name != null) it[Customers.name] = name
                        if (email != null) it[Customers.email] = email
                    }
                    .singleOrNull()
                    ?.toCustomer()
            }
        }

    suspend fun deleteCustomer(id: Int): Boolean =
        withContext(Dispatchers.IO) {
            transaction(database) { Customers.deleteWhere { Customers.id eq id } > 0 }
        }

    private fun ResultRow.toCustomer(): Customer =
        Customer(
            id = this[Customers.id].value,
            name = this[Customers.name],
            email = this[Customers.email],
            createdAt = this[Customers.createdAt],
        )
}
