package org.jetbrains.ktor.sample.ai

import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.config.AIConfig
import org.jetbrains.ktor.sample.config.AiModule
import kotlin.test.Test
import java.io.ByteArrayOutputStream
import java.time.Duration
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DocumentServiceTest : DatabaseSpec() {
    private val config = AIConfig(
        "http://localhost:11434",
        "not-need-for-llama.cpp",
        "llama3.2",
        "all-minilm-l6-v2-q-tokenizer.json",
        550,
        50
    )
    private val memory by lazy { ExposedChatMemoryStore(database) }
    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val module by lazy {
        AiModule(
            config, memory, OllamaStreamingChatModel.builder()
                .baseUrl(config.baseUrl)
                .modelName(config.model)
                .temperature(0.7)
                .timeout(Duration.ofMinutes(2))
                .build()
        )
    }
    private val documentService by lazy { DocumentService(module.ingestor, registry) }

    @Test
    fun testIngestPdfDocument() = runBlocking {
        val pdfBytes = createTestPdf("This is a test PDF document for embedding.")
        val result = documentService.ingestPdfs(pdfBytes).toList()
        assert(result.size == 1)
    }

    private fun createTestPdf(content: String): Flow<File> = flow {
        PDDocument().use { document ->
            val page = PDPage()
            document.addPage(page)

            PDPageContentStream(document, page).use { contentStream ->
                contentStream.beginText()
                contentStream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
                contentStream.newLineAtOffset(100f, 700f)
                contentStream.showText(content)
                contentStream.endText()
            }

            val file = createTempFile().toFile()
            try {
                document.save(file)
                emit(file)
            } finally {
                file.delete()
            }
        }
    }
}
