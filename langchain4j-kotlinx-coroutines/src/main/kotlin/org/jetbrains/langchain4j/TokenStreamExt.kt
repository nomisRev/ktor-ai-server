package org.jetbrains.langchain4j

import dev.langchain4j.model.chat.StreamingChatLanguageModelReply
import dev.langchain4j.service.TokenStream
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

fun TokenStream.asFlow(): Flow<String> = channelFlow {
    onPartialResponse { trySendBlocking(it) }
    onCompleteResponse { close() }
    onError { close(it) }
    start()
    awaitClose()
}

fun TokenStream.asReplyFlow(): Flow<StreamingChatLanguageModelReply> =
    channelFlow<StreamingChatLanguageModelReply> {
        onPartialResponse { token ->
            trySendBlocking(StreamingChatLanguageModelReply.PartialResponse(token))
        }
        onCompleteResponse { response ->
            trySendBlocking(StreamingChatLanguageModelReply.CompleteResponse(response))
            close()
        }
        onError { throwable ->
            StreamingChatLanguageModelReply.Error(throwable)
            close(throwable)
        }
        start()
        awaitClose()
    }
