# Kotlin Project Guidelines

## Project Overview
This is a Ktor-based project that follows modern Kotlin development practices, emphasizing clean code, type safety, and immutability.

## Coding Standards

### General Principles
- Write referentially transparent code
- Keep code concise and focused
- Follow idiomatic Kotlin practices
- Prioritize readability and maintainability

### Specific Guidelines

#### 1. Immutability First
- Use `val` instead of `var` by default
- Only use `var` when state mutation is absolutely necessary
- Prefer immutable collections:
  ```kotlin
  // Preferred
  val items: List<String> = listOf("a", "b", "c")
  val uniqueItems: Set<String> = setOf("a", "b", "c")
  val mappings: Map<String, Int> = mapOf("a" to 1, "b" to 2)

  // Avoid
  val items: MutableList<String> = mutableListOf()
  var mappings: MutableMap<String, Int> = mutableMapOf()
  ```

#### 2. Data Representation
- Use data classes for representing state
- Keep data classes focused and immutable
  ```kotlin
  // Good
  data class User(
      val id: String,
      val name: String,
      val email: String
  )

  // Avoid
  class User {
      var id: String = ""
      var name: String = ""
      var email: String = ""
  }
  ```

#### 3. Functional Approaches
- Prefer extension functions for adding functionality
- Use higher-order functions when appropriate
- Leverage Kotlin's scope functions (let, run, with, apply, also) appropriately
- Avoid unnecessary usage of scope functions that introduce redundant nesting
  ```kotlin
  // Avoid: Unnecessary let usage
  client.get("/").let { response ->
      assertEquals(HttpStatusCode.OK, response.status)
      assertEquals("Hello, World!", response.bodyAsText())
  }

  // Preferred: Direct assignment
  val response = client.get("/")
  assertEquals(HttpStatusCode.OK, response.status)
  assertEquals("Hello, World!", response.bodyAsText())
  ```

#### 4. Error Handling
- Use sealed classes for error handling
- Prefer Result type for operations that can fail
- Avoid throwing exceptions for control flow

### Dependencies

#### Framework and Libraries
- Use Ktor for server-side development
- Prefer KotlinX libraries where available
- Use Version Catalog for dependency management

#### Gradle Configuration
- Use Kotlin Gradle Script (build.gradle.kts)
- Maintain versions in the Version Catalog (libs.versions.toml)
- Do not downgrade dependencies manually

### Architecture Principles
- Keep modules focused and cohesive
- Follow SOLID principles
- Maintain clear separation of concerns
- Use dependency injection where appropriate

### Testing
- Write unit tests for business logic
- Use Ktor's testing utilities for integration tests
- Follow TDD practices where possible

## Code Examples

### Preferred Patterns

```kotlin
// Data representation
data class Configuration(
    val host: String,
    val port: Int,
    val security: SecurityConfig
)

// Extension functions
fun String.toSlug() = lowercase()
    .replace(Regex("[^a-z0-9\\s-]"), "")
    .replace(Regex("[-\\s]+"), "-")

// Error handling
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
```

### Anti-patterns to Avoid

```kotlin
// Avoid: Mutable state
var globalConfig: Configuration? = null

// Avoid: Unnecessary mutability
class UserRepository {
    private val users = mutableListOf<User>()
    fun addUser(user: User) {
        users.add(user)
    }
}
```

```kotlin
// Avoid many statements
assertNotNull(user)
assertEquals("Test User", user.name)
assertEquals("test@example.com", user.email)
assertEquals("USER", user.role)
assertNotNull(user.expiresAt)

// Prefer instead

```
