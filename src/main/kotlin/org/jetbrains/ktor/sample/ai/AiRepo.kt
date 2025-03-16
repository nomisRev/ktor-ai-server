package org.jetbrains.ktor.sample.ai

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.Tokenizer
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonObject

interface Chat {
    fun answer(question: String): String
}

class AiRepo(config: AIConfig) : Chat {
    private val store = InMemoryEmbeddingStore<TextSegment>()
    private val embeddingModel = AllMiniLmL6V2QuantizedEmbeddingModel()

    private val contentRetriever: ContentRetriever =
        EmbeddingStoreContentRetriever.builder()
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

    private val tokenizer = AllMiniLmL6V2Tokenizer()
    private val splitter = DocumentSplitters.recursive(550, 50, tokenizer)

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

class AllMiniLmL6V2Tokenizer : Tokenizer {
    private val tokenizer: HuggingFaceTokenizer

    init {
        val tokenizerConfig = Json {
            isLenient
        }.decodeFromStream<JsonElement>(AiRepo::class.java.classLoader.getResourceAsStream("all-minilm-l6-v2-q-tokenizer-config.json"))
        val config = buildMap {
            put("padding", "false")
            put("truncation", "false")
            putAll(tokenizerConfig.toMap())
        }
        tokenizer = HuggingFaceTokenizer.newInstance(
            AiRepo::class.java.classLoader.getResourceAsStream("all-minilm-l6-v2-q-tokenizer.json"),
            config
        )
    }

    fun JsonElement.toMap(): Map<String, String?> =
        jsonObject.mapValues { (key, value) ->
            when (value) {
                is JsonArray -> throw IllegalArgumentException("Arrays are not supported")
                is JsonObject -> throw IllegalArgumentException("Objects are not supported")
                is JsonPrimitive -> value.content
                JsonNull -> null
            }
        }

    override fun estimateTokenCountInText(text: String): Int =
        tokenizer.encode(text, false, true).tokens.size

    override fun estimateTokenCountInMessage(message: ChatMessage): Int =
        estimateTokenCountInText(message.text())

    override fun estimateTokenCountInMessages(messages: Iterable<ChatMessage>): Int =
        messages.sumOf { estimateTokenCountInMessage(it) }
}