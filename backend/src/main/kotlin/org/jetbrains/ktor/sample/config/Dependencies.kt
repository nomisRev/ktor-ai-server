package org.jetbrains.ktor.sample.config

import io.ktor.server.application.Application
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.ktor.sample.ai.AiService
import org.jetbrains.ktor.sample.ai.DocumentService
import org.jetbrains.ktor.sample.ai.ExposedChatMemoryStore
import org.jetbrains.ktor.sample.booking.BookingService
import org.jetbrains.ktor.sample.booking.CustomerService

class Dependencies(
    val ai: Deferred<AiService>, 
    val documentService: Deferred<DocumentService>,
    val customerService: Deferred<CustomerService>,
    val bookingService: Deferred<BookingService>
)

fun Application.dependencies(config: AppConfig): Dependencies {
    val database = setupDatabase(config.database, config.flyway)
    val registry = setupMetrics()
    val aiModule = async(Dispatchers.IO) { AiModule(config.ai, ExposedChatMemoryStore(database)) }

    // Initialize booking services
    val customerService = async(Dispatchers.IO) { CustomerService(database) }
    val bookingService = async(Dispatchers.IO) { BookingService(database) }

    return Dependencies(
        ai = async(Dispatchers.IO) { AiService(aiModule.await(), registry) },
        documentService = async(Dispatchers.IO) { DocumentService(aiModule.await().ingestor, registry) },
        customerService = customerService,
        bookingService = bookingService
    )
}
