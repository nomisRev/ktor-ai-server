package org.jetbrains.ktor.sample.ai

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore

interface Chat {
    fun answer(question: String): String
}

class AiRepo(config: AIConfig) : Chat {
    private val store = InMemoryEmbeddingStore<TextSegment>()
    private val embeddingModel = AllMiniLmL6V2QuantizedEmbeddingModel()
    private val tokenizer = HuggingFaceTokenizer(config.tokenizer)
    private val splitter =
        DocumentSplitters.recursive(config.maxSegmentSizeInTokens, config.maxOverlapSizeInTokens, tokenizer)

    private val contentRetriever = EmbeddingStoreContentRetriever.builder()
        .embeddingStore(store)
        .embeddingModel(embeddingModel)
        .maxResults(5)
        .minScore(0.5)
        .build()

    private val chatModel = OpenAiChatModel.builder()
        .baseUrl(config.baseUrl)
        .apiKey(config.apiKey)
        .logRequests(true)
        .logResponses(true)
        .modelName(config.model)
        .temperature(0.7)
        .build()

    private val chatMemory = MessageWindowChatMemory
        .builder()
        .maxMessages(10)
        .build()

    private val chat: Chat = AiServices.builder<Chat>(Chat::class.java)
        .chatLanguageModel(chatModel)
        .chatMemory(chatMemory)
        .contentRetriever(contentRetriever)
        .build()

    private val ingestor = EmbeddingStoreIngestor.builder()
        .embeddingStore(store)
        .embeddingModel(embeddingModel)
        .documentSplitter(splitter)
        .build()

    fun loadDocuments() {
        val text = AiRepo::class.java.classLoader.getResourceAsStream("test_content.txt").bufferedReader()
            .use { it.readText() }
        val doc = Document.document(text)
        ingestor.ingest(doc)
    }

    override fun answer(prompt: String): String = chat.answer(prompt)
}
