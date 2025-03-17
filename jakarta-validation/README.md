# Jakarta Validation Plugin for Ktor

A Ktor plugin that integrates Jakarta Validation (formerly Bean Validation) with Ktor applications. This plugin allows
you to validate incoming request bodies using Jakarta Validation annotations.

## Features

- Validates request bodies using Jakarta Validation annotations
- Customizable error handling
- Content length validation

## Installation

- Release in process 

## Usage

### Basic Usage

```kotlin
fun Application.module() {
    // All configuration options shown below are optional and shown with default values.
    install(JakartaValidation) {
        validateContentLength()
        errorHandler { violations ->
            respond(HttpStatusCode.BadRequest, "${violations.joinToString { it.message }}")
        }
        factory(
            Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(ParameterMessageInterpolator())
                .buildValidatorFactory()
        )
    }

    routing {
        post("/user") {
            val user = call.receive<User>() // Validation happens here
            // Process the validated user
            call.respond(HttpStatusCode.Created, user)
        }
    }
}

@Serializable
data class User(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:Email(message = "Invalid email format")
    val email: String,

    @field:Min(value = 18, message = "Age must be at least 18")
    val age: Int,

    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    val password: String
)
```

The plugin intercepts the request pipeline and validates the request body using Jakarta Validation. If validation fails,
it responds with a 400 Bad Request status code and the validation error messages.

## License

This project is licensed under the Apache License 2.0.
