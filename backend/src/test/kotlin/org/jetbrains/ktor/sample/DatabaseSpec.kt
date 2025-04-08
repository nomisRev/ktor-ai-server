package org.jetbrains.ktor.sample

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.ktor.sample.config.dataSource
import org.jetbrains.ktor.sample.config.flyway
import org.junit.ClassRule
import org.junit.rules.ExternalResource

abstract class DatabaseSpec {
    val database by Companion.database

    companion object {
        @JvmStatic
        @get:ClassRule
        val dataSource =
            autoClose({
                dataSource(AppTestConfig.database).also { flyway(it, AppTestConfig.flyway) }
            })

        @JvmStatic
        @get:ClassRule
        val database =
            autoClose({
                val ds by dataSource
                Database.connect(ds)
            }) {
                TransactionManager.closeAndUnregister(it)
            }
    }
}

class AutoCloseResource<A>(private val initializer: () -> A, private val close: (A) -> Unit) :
    ExternalResource(), ReadOnlyProperty<Any?, A> {
    private var _value: A? = null
    val value: A
        get() = requireNotNull(_value) { "Resource is not initialized yet!" }

    override fun before() {
        super.before()
        require(_value == null) { "Resource is already initialized!" }
        _value = initializer()
    }

    override fun after() {
        super.after()
        close(requireNotNull(_value) { "Resource was not initialized!" })
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): A =
        requireNotNull(_value) { "Resource is not initialized yet!" }
}

fun <A> autoClose(initializer: () -> A, close: (A) -> Unit): AutoCloseResource<A> =
    AutoCloseResource(initializer, close)

fun <A : AutoCloseable> autoClose(initializer: () -> A): AutoCloseResource<A> =
    AutoCloseResource(initializer) { it.close() }
