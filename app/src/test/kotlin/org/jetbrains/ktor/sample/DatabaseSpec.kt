package org.jetbrains.ktor.sample

import kotlin.test.BeforeTest
import kotlin.test.Test
import org.jetbrains.exposed.sql.Database
import org.jetbrains.ktor.sample.config.dataSource
import org.jetbrains.ktor.sample.config.flyway
import org.junit.ClassRule
import org.junit.rules.ExternalResource

abstract class DatabaseSpec {
    lateinit var database: Database

    @BeforeTest
    fun setup() {
        val config = AppTestConfig.database
        val dataSource = dataSource(config)
        flyway(dataSource, AppTestConfig.flyway)
        database = Database.connect(dataSource)
    }
}

class CloseableExternalResource<A>(val initializer: () -> A, val close: (A) -> Unit) :
    ExternalResource() {
    override fun before() {
        super.before()
        println("before")
    }

    override fun after() {
        super.after()
        println("after")
    }
}

class Test {
    @Test
    fun boo() {
        println("boo")
        assert(true)
    }

    @Test
    fun foo() {
        println("foo")
        assert(true)
    }

    companion object {
        @JvmStatic @get:ClassRule val resource = CloseableExternalResource({}, {})
    }
}
