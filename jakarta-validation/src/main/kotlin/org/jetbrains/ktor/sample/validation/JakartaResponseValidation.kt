package org.jetbrains.ktor.sample.validation

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.ResponseBodyReadyForSend
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.response.ApplicationSendPipeline
import io.ktor.server.response.respond
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory

/**
 * Configuration for the Jakarta Response Validation plugin.
 */
class JakartaResponseValidationConfig {
    internal var validatorFactory: ValidatorFactory = Validation.byDefaultProvider()
        .configure()
        .buildValidatorFactory()

    internal var errorHandler: suspend ApplicationCall.(Set<ConstraintViolation<*>>) -> Unit = { violations ->
        val errors = violations.joinToString { it.message }
        respond(HttpStatusCode.InternalServerError, errors)
    }

    /**
     * Sets a custom validator factory.
     */
    fun factory(factory: ValidatorFactory) {
        validatorFactory = factory
    }

    /**
     * Sets a custom error handler for validation failures.
     */
    fun errorHandler(handler: suspend ApplicationCall.(Set<ConstraintViolation<*>>) -> Unit) {
        errorHandler = handler
    }
}

/**
 * A plugin that validates response objects using Jakarta Validation.
 */
val JakartaResponseValidation = createApplicationPlugin(
    name = "JakartaResponseValidation",
    createConfiguration = ::JakartaResponseValidationConfig
) {
    val validator = pluginConfig.validatorFactory.validator

    // Validate response objects before they're sent to the client
    onCall { call ->
        val config = pluginConfig
        val localValidator = validator

        // Intercept the response pipeline at the Before phase
        call.response.pipeline.intercept(ApplicationSendPipeline.Before) { payload ->
            // Skip validation for primitive types and null values
            if (payload == null || payload is String || payload is Number || payload is Boolean) {
                return@intercept
            }

            // Validate the response body
            val failures = localValidator.validate(payload)

            if (failures.isNotEmpty()) {
                config.errorHandler(call, failures)
                finish()
                throw JValidationException()
            }
        }
    }
}
