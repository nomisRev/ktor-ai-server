package io.github.nomisrev.openai

import io.ktor.client.call.body
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.append
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.core.BytePacketBuilder
import io.ktor.utils.io.core.ByteReadPacket
import kotlin.Any
import kotlin.Enum
import kotlin.Long
import kotlin.OptIn
import kotlin.Pair
import kotlin.Result
import kotlin.String
import kotlin.Unit
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KClass
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializerOrNull
import kotlin.jvm.Throws

public suspend inline fun <reified A> HttpResponse.bodyOrThrow(): A = try {
    body<A>()
        ?: throw SerializationException("Body of ${A::class.simpleName} expected, but found null")
} catch (e: IllegalArgumentException) {
    val requestBody =
        when (val content = request.content) {
            is OutgoingContent.ByteArrayContent ->
                kotlin.runCatching { content.bytes().decodeToString() }
                    .getOrElse { "ByteArrayContent (non-UTF8)" }
            is OutgoingContent.NoContent -> "NoContent"
            is OutgoingContent.ProtocolUpgrade -> "ProtocolUpgrade"
            is OutgoingContent.ReadChannelContent -> "ReadChannelContent"
            is OutgoingContent.WriteChannelContent -> "WriteChannelContent"
            else -> "UnknownContent"
        }
    val bodyAsText = kotlin.runCatching { bodyAsText() }.getOrNull()
    throw SerializationException(
        """
    |Failed to serialize response body to ${A::class.simpleName}
    |Request URL: ${request.url}
    |Request Method: ${request.method}
    |Request Body: $requestBody
    |Response Status: $status
    |Response Headers: $headers
    |Response bodyAsText: $bodyAsText
  """
            .trimMargin(),
        e
    )
} catch (e: Throwable) {
    throw e
}

public fun requireAll(vararg requires: () -> Unit) {
    requires.map { require ->
        runCatching { require() }
    }.throwIfNeeded()
}

public fun Iterable<Result<*>>.throwIfNeeded() {
    val throwables =
        mapNotNull(Result<*>::exceptionOrNull)

    if (throwables.isNotEmpty()) {
        val errors = throwables
            .mapNotNull { it.message }
            .joinToString("\n") { "  - $it" }

        val cause = throwables.reduce { acc, other ->
            acc.apply { other.let(::addSuppressed) }
        }

        throw IllegalArgumentException("Requirements not met:\n$errors", cause)
    }
}

public class UnionSerializationException(
    public val payload: JsonElement,
    public val errors: Map<KClass<*>, SerializationException>,
) : SerializationException() {
    override val message: String = """
        Failed to deserialize Json: $payload.
        Errors: ${
        errors.entries.joinToString(separator = "\n") { (type, error) ->
            "$type - failed to deserialize: ${error.stackTraceToString()}"
        }
    }
        """.trimIndent()
}

public fun <A> attemptDeserialize(json: JsonElement, vararg
block: Pair<KClass<*>, (JsonElement) -> A>): A {
    val errors = linkedMapOf<KClass<*>, SerializationException>()
    block.forEach { (kclass, f) ->
        try {
            return f(json)
        } catch (e: SerializationException) {
            errors[kclass] = e
        }
    }
    throw UnionSerializationException(json, errors)
}

public fun <A> deserializeOpenEnum(
    `value`: String,
    `open`: (String) -> A,
    vararg block: Pair<KClass<*>, (String) -> A?>,
): A {
    val errors = linkedMapOf<KClass<*>, SerializationException>()
    block.forEach { (kclass, f) ->
        try {
            f(value)?.let { res -> return res }
        } catch (e: SerializationException) {
            errors[kclass] = e
        }
    }
    return open(value)
}

public data class UploadFile(
    public val filename: String,
    public val bodyBuilder: BytePacketBuilder.() -> Unit,
    public val contentType: ContentType? = null,
    public val size: Long? = null,
)

public fun <T : Any> FormBuilder.appendAll(
    key: String,
    `value`: T?,
    headers: Headers = Headers.Empty,
) = when (value) {
        is String -> append(key, value, headers)
        is Number -> append(key, value, headers)
        is Boolean -> append(key, value, headers)
        is ByteArray -> append(key, value, headers)
        is ByteReadPacket -> append(key, value, headers)
        is InputProvider -> append(key, value, headers)
        is ChannelProvider -> append(key, value, headers)
        is UploadFile -> appendUploadedFile(key, value)
        is Enum<*> -> append(key, serialNameOrEnumValue(value), headers)
        null -> Unit
        else -> append(FormPart(key, value, headers))
    }

private fun FormBuilder.appendUploadedFile(key: String, `file`: UploadFile) {
    append(
        key = key,
        filename = file.filename,
        contentType = file.contentType ?: ContentType.Application.OctetStream,
        size = file.size,
        bodyBuilder = file.bodyBuilder
    )
}

@OptIn(
    ExperimentalSerializationApi::class,
    InternalSerializationApi::class,
)
private fun <T : Enum<T>> serialNameOrEnumValue(`enum`: Enum<T>): String =
    enum::class.serializerOrNull()?.descriptor?.getElementName(enum.ordinal) ?: enum.toString()
