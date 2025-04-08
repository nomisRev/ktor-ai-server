package org.jetbrains.ktor.sample.ai

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.IngestionResult
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.TimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.jetbrains.ktor.sample.track

class DocumentService(private val ingestor: EmbeddingStoreIngestor, registry: MeterRegistry) {
    private val documentLoadTimer =
        Timer.builder("ai.document.load.time")
            .description("Time taken to load a document")
            .register(registry)

    private val parser = ApachePdfBoxDocumentParser()

    suspend fun ingestDocument(content: String): IngestionResult =
        withContext(Dispatchers.IO) {
            measureDocumentLoadTime {
                val doc = Document.document(content)
                ingestor.ingest(doc)
            }
        }

    private fun ingestPdf(file: File): Flow<IngestionResult> = flow {
        val document = parser.parse(file.inputStream())
        emit(ingestor.ingest(document))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun ingestPdfs(files: Flow<File>): Flow<IngestionResult> =
        files
            .flatMapMerge { file -> ingestPdf(file) }
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
