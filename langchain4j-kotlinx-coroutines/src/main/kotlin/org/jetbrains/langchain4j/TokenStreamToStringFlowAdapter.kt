package org.jetbrains.langchain4j

import dev.langchain4j.service.TokenStream
import dev.langchain4j.spi.services.TokenStreamAdapter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class TokenStreamToStringFlowAdapter : TokenStreamAdapter {
    override fun canAdaptTokenStreamTo(type: Type?): Boolean {
        if (type is ParameterizedType) {
            if (type.rawType === Flow::class.java) {
                val typeArguments: Array<Type?> = type.actualTypeArguments
                return typeArguments.size == 1 && typeArguments[0] === String::class.java
            }
        }
        return false
    }

    override fun adapt(tokenStream: TokenStream): Any =
        tokenStream.asFlow()
}
