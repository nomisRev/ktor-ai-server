package org.jetbrains.ktor.sample.validation

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import java.io.IOException

val JakartaValidation: RouteScopedPlugin<JakartaValidationConfig> =
    createRouteScopedPlugin("JakartaValidation", ::JakartaValidationConfig) {
        val validator = pluginConfig.validatorFactory.validator

        on(RequestBodyTransformed) { pipeline, content ->
            val failures = validator.validate(content)
            if (failures.isNotEmpty()) {
                pluginConfig.errorHandler(pipeline.call, failures)
                pipeline.finish()
                throw JValidationException()
            }
        }

        // TODO: Verify this is correct
        on(RenderResponse) { pipeline, content ->
            if (content is String || content is Number || content is Boolean || content is HttpStatusCode) return@on
            val failures = validator.validate(content)
            if (failures.isNotEmpty()) {
                pluginConfig.responseErrorHandler(pipeline.call, failures)
                pipeline.finish()
                throw JValidationException()
            }
        }

        if (pluginConfig.validateContentLength) {
            on(ReceiveRequestBytes) { call, body ->
                val contentLength = call.request.contentLength() ?: return@on body

                application.writer {
                    val count = body.copyTo(channel)
                    if (count != contentLength) throw IOException("Content length mismatch. Actual $count, expected $contentLength.")
                }.channel
            }
        }
    }

private object RequestBodyTransformed :
    Hook<suspend (context: PipelineContext<Any, PipelineCall>, content: Any) -> Unit> {
    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (context: PipelineContext<Any, PipelineCall>, content: Any) -> Unit
    ) {
        pipeline.receivePipeline.intercept(ApplicationReceivePipeline.After) {
            handler(this, subject)
        }
    }
}

private object RenderResponse : Hook<suspend (context: PipelineContext<Any, PipelineCall>, content: Any) -> Unit> {
    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (context: PipelineContext<Any, PipelineCall>, content: Any) -> Unit
    ) {
        pipeline.sendPipeline.intercept(ApplicationSendPipeline.Before) {
            handler(this, subject)
        }
    }
}

class JakartaValidationConfig {
    internal var validatorFactory: ValidatorFactory =
        Validation.byDefaultProvider()
            .configure()
            .buildValidatorFactory()

    internal var errorHandler: suspend ApplicationCall.(Set<ConstraintViolation<*>>) -> Unit = { violations ->
        val errors = violations.joinToString { it.message }
        respond(HttpStatusCode.BadRequest, errors)
    }

    internal var responseErrorHandler: suspend ApplicationCall.(Set<ConstraintViolation<*>>) -> Unit = { violations ->
        respond(HttpStatusCode.InternalServerError)
    }

    internal var validateContentLength: Boolean = false

    fun validateContentLength() {
        validateContentLength = true
    }

    fun factory(factory: ValidatorFactory) {
        validatorFactory = factory
    }

    fun errorHandler(handler: suspend ApplicationCall.(Set<ConstraintViolation<*>>) -> Unit) {
        errorHandler = handler
    }

    fun responseErrorHandler(handler: suspend ApplicationCall.(Set<ConstraintViolation<*>>) -> Unit) {
        responseErrorHandler = handler
    }
}

// We extend CancellationException because this should never be caught in Kotlin,
// and it's not logged by Ktor.
class JValidationException : CancellationException(
    "Ktor JakartaValidation plugin exception: this exception should not be caught. The Validation plugin has already responded with BadRequest."
)
