package org.jetbrains.ktor.sample.config

import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.Tokenizer
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import io.ktor.server.application.ApplicationEnvironment
import kotlin.reflect.KClass

data class AIConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val tokenizer: String,
    val maxSegmentSizeInTokens: Int,
    val maxOverlapSizeInTokens: Int,
) {
    companion object {
        fun load(environment: ApplicationEnvironment): AIConfig = with(environment.config) {
            AIConfig(
                baseUrl = property("ai.baseUrl").getString(),
                apiKey = property("ai.apiKey").getString(),
                model = property("ai.model").getString(),
                tokenizer = property("ai.tokenizer").getString(),
                maxSegmentSizeInTokens = property("ai.maxSegmentSizeInTokens").getString().toInt(),
                maxOverlapSizeInTokens = property("ai.maxOverlapSizeInTokens").getString().toInt()
            )
        }
    }
}

class AiModule(
    config: AIConfig,
    memoryStore: ChatMemoryStore,
    private val model: StreamingChatLanguageModel
) {
    private val store: EmbeddingStore<TextSegment> = InMemoryEmbeddingStore<TextSegment>()
    private val embeddings: EmbeddingModel = AllMiniLmL6V2QuantizedEmbeddingModel()
    private val tokenizer: Tokenizer = HuggingFaceTokenizer(config.tokenizer)
    private val splitter: DocumentSplitter =
        DocumentSplitters.recursive(config.maxSegmentSizeInTokens, config.maxOverlapSizeInTokens, tokenizer)

    private val retriever: EmbeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
        .embeddingStore(store)
        .embeddingModel(embeddings)
        .maxResults(5)
        .minScore(0.5)
        .build()

    private val memory: ChatMemoryProvider = ChatMemoryProvider { memoryId: Any? ->
        MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(10)
            .chatMemoryStore(memoryStore)
            .build()
    }

    val ingestor: EmbeddingStoreIngestor = EmbeddingStoreIngestor.builder()
        .embeddingStore(store)
        .embeddingModel(embeddings)
        .documentSplitter(splitter)
        .build()

    fun <A : Any> aiServices(kClass: KClass<A>): AiServices<A> =
        AiServices.builder<A>(kClass.java)
            .streamingChatLanguageModel(model)
            .chatMemoryProvider(memory)
            .contentRetriever(retriever)
}

inline fun <reified A : Any> AiModule.services(): A =
    aiServices(A::class).build()
