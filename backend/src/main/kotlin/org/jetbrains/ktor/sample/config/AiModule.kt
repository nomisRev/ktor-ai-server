package org.jetbrains.ktor.sample.config

import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

@Serializable
data class AIConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val tokenizer: String,
    val maxSegmentSizeInTokens: Int,
    val maxOverlapSizeInTokens: Int,
)

class AiModule(config: AIConfig, memoryStore: ChatMemoryStore) {
    private val model: StreamingChatLanguageModel =
        OpenAiStreamingChatModel.builder().apiKey(config.apiKey).modelName(config.model).build()

    private val store: EmbeddingStore<TextSegment> = InMemoryEmbeddingStore()
    private val embeddings: EmbeddingModel = AllMiniLmL6V2QuantizedEmbeddingModel()
    private val splitter: DocumentSplitter =
        DocumentSplitters.recursive(config.maxSegmentSizeInTokens, config.maxOverlapSizeInTokens)

    private val retriever: EmbeddingStoreContentRetriever =
        EmbeddingStoreContentRetriever.builder()
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

    val ingestor: EmbeddingStoreIngestor =
        EmbeddingStoreIngestor.builder()
            .embeddingStore(store)
            .embeddingModel(embeddings)
            .documentSplitter(splitter)
            .build()

    fun <A : Any> service(kClass: KClass<A>): A =
        AiServices.builder<A>(kClass.java)
            .streamingChatLanguageModel(model)
            .chatMemoryProvider(memory)
            .contentRetriever(retriever)
            .build()

    inline fun <reified A : Any> service(): A = service(A::class)
}
