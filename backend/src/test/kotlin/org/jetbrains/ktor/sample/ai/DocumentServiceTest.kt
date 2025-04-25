package org.jetbrains.ktor.sample.ai

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.jetbrains.ktor.sample.DatabaseSpec
import org.jetbrains.ktor.sample.config.AIConfig
import org.jetbrains.ktor.sample.config.AiModule

class DocumentServiceTest : DatabaseSpec() {
    private val config =
        AIConfig(
            "http://localhost:11434",
            "not-need-for-llama.cpp",
            "llama3.2",
            "all-minilm-l6-v2-q-tokenizer.json",
            550,
            50,
        )
    private val memory by lazy { ExposedChatMemoryStore(database) }
    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val module by lazy { AiModule(config, memory) }
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
                //
                // contentStream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
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
