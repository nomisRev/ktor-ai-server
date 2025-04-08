package org.jetbrains.ktor.sample.ai

import dev.langchain4j.data.document.Document
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.IngestionResult
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.TimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.jetbrains.ktor.sample.track

class DocumentService(private val ingestor: EmbeddingStoreIngestor, registry: MeterRegistry) {
    private val documentLoadTimer =
        Timer.builder("ai.document.load.time")
            .description("Time taken to load a document")
            .register(registry)

    suspend fun ingestDocument(content: String): IngestionResult =
        withContext(Dispatchers.IO) {
            measureDocumentLoadTime {
                val doc = Document.document(content)
                ingestor.ingest(doc)
            }
        }

    fun ingestPdfs(files: Flow<File>): Flow<IngestionResult> =
        files
            .map { file ->
                Loader.loadPDF(file).use { document ->
                    val text = PDFTextStripper().getText(document)
                    val doc = Document.document(text)
                    ingestor.ingest(doc)
                }
            }
            .flowOn(Dispatchers.IO)
            .track(::measureDocumentLoadTime)

    private suspend fun <A> measureDocumentLoadTime(block: suspend () -> A): A {
        val mark = TimeSource.Monotonic.markNow()
        return try {
            block()
        } finally {
            documentLoadTimer.record(mark.elapsedNow().inWholeMilliseconds, TimeUnit.MILLISECONDS)
        }
    }
}
