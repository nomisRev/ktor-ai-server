package org.jetbrains.ktor.sample.ai

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.Tokenizer
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.store.memory.chat.ChatMemoryStore


private const val SYSTEM_MESSAGE: String =
    """You are an AI assistant for a travel agency. Your role is to provide helpful, accurate, 
and personalized travel information to customers. You should:

1. Provide detailed information about destinations, accommodations, transportation options, 
   and activities based on the customer's interests and preferences.
2. Consider factors like budget, travel dates, group size, and special requirements when 
   making recommendations.
3. Offer practical travel tips and advice relevant to the destinations being discussed.
4. Be knowledgeable about travel regulations, visa requirements, and safety considerations.
5. Maintain a friendly, professional tone that inspires confidence in your recommendations.
"""

interface Chat {
    @SystemMessage(SYSTEM_MESSAGE)
    @UserMessage()
    fun answer(@MemoryId userId: Long, question: String): String
}

// TODO: proper DI, we don't need AIRepo.
//   Split document loading from `Chat`, and only expose `Chat` + fun interface DocumentLoader to Application.
class AiRepo(
    config: AIConfig,
    memoryStore: ChatMemoryStore,
    private val metrics: AiMetrics,
    private val embeddingStore: EmbeddingStore<TextSegment> = InMemoryEmbeddingStore<TextSegment>(),
    private val embeddingModel: EmbeddingModel = AllMiniLmL6V2QuantizedEmbeddingModel(),
    private val tokenizer: Tokenizer = HuggingFaceTokenizer(config.tokenizer),
    private val splitter: DocumentSplitter =
        DocumentSplitters.recursive(config.maxSegmentSizeInTokens, config.maxOverlapSizeInTokens, tokenizer),
    private val contentRetriever: EmbeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
        .embeddingStore(embeddingStore)
        .embeddingModel(embeddingModel)
        .maxResults(5)
        .minScore(0.5)
        .build(),
    private val chatModel: ChatLanguageModel = OpenAiChatModel.builder()
        .baseUrl(config.baseUrl)
        .apiKey(config.apiKey)
        .logRequests(true)
        .logResponses(true)
        .modelName(config.model)
        .temperature(0.7)
        .build(),
    private val chatMemoryProvider: ChatMemoryProvider = ChatMemoryProvider { memoryId: Any? ->
        MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(10)
            .chatMemoryStore(memoryStore)
            .build()
    },
    private val chat: Chat = AiServices.builder<Chat>(Chat::class.java)
        .chatLanguageModel(chatModel)
        .chatMemoryProvider(chatMemoryProvider)
        .contentRetriever(contentRetriever)
        .build(),
    private val ingestor: EmbeddingStoreIngestor = EmbeddingStoreIngestor.builder()
        .embeddingStore(embeddingStore)
        .embeddingModel(embeddingModel)
        .documentSplitter(splitter)
        .build()
) : Chat {
    fun loadDocuments() {
        metrics.measureDocumentLoadTime {
            metrics?.measureDocumentLoadTime {
                val text = AiRepo::class.java.classLoader.getResourceAsStream("test_content.txt").bufferedReader()
                    .use { it.readText() }
                val doc = Document.document(text)
                ingestor.ingest(doc)
            }
        }
    }

    override fun answer(userId: Long, question: String): String =
        metrics.measureQuestionAnswerTime {
            chat.answer(userId, question)
        }
}
