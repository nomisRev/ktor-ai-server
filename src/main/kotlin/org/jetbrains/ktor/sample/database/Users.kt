package org.jetbrains.ktor.sample.database

import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable("users", "user_id") {
    val name = varchar("name", 50)
    val email = varchar("email", 100)
}