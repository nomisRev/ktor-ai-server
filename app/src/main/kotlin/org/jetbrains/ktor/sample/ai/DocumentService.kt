package org.jetbrains.ktor.sample.ai

import dev.langchain4j.data.document.Document
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.IngestionResult
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit
import kotlin.time.TimeSource

class DocumentService(
    private val ingestor: EmbeddingStoreIngestor,
    registry: MeterRegistry,
) {
    private val documentLoadTimer = Timer
        .builder("ai.document.load.time")
        .description("Time taken to load a document")
        .register(registry)

    fun loadTestDocuments() {
        measureDocumentLoadTime {
            val text = AiService::class.java.classLoader.getResourceAsStream("test_content.txt").bufferedReader()
                .use { it.readText() }
            val doc = Document.document(text)
            ingestor.ingest(doc)
        }
    }

    fun ingestDocument(content: String): IngestionResult =
        measureDocumentLoadTime {
            val doc = Document.document(content)
            ingestor.ingest(doc)
        }

    fun <A> measureDocumentLoadTime(block: () -> A): A {
        val mark = TimeSource.Monotonic.markNow()
        return try {
            block()
        } finally {
            documentLoadTimer.record(mark.elapsedNow().inWholeMilliseconds, TimeUnit.MILLISECONDS)
        }
    }
}