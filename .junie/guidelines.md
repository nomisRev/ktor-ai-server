# Guidelines for Junie

**Very important:** strictly follow the rules below:

- Write down the timestamp of the session start (timestamp format: yyyy-MM-dd_hh_mm)
- Before generating any code, always generate a plan-{timestamp}.md file.
- Use the plan-{timestamp}.md file as an input to generate the detailed enumerated task list.
- Store the task list to tasks-{timestamp}.md file
- Do the work that was submitted with the user prompt
- After each task is done, mark it as completed in the task-{timestamp}.md file

## General Principles

- Write referentially transparent code
- Keep code concise and focused
- Follow idiomatic Kotlin practices
- Prioritize readability and maintainability
- Never leave unused imports
- Use List instead of MutableList, etc
- Use `val` instead of `var` by default
- Only use `var` when state mutation is necessary
- Use data classes for representing state
- Keep data classes focused and immutable
- Avoid throwing exceptions for control flow
- Prefer KotlinX libraries where available
- Maintain versions in the Version Catalog (libs.versions.toml)
- Use Kotlin Gradle Script (build.gradle.kts)
- Do not downgrade dependencies manually
- Maintain a clear separation of concerns
- Use dependency injection where appropriate
- Write unit tests for business logic
- Use Ktor's testing utilities for integration tests
- Follow TDD practices where possible
- Try to write as many failing tests as you can before fixing them
- Avoid writing many assertions in a test and try to keep the test small
- Use runBlocking only in tests, not production code.
- Follow structured concurrency (e.g., supervisorScope for fault isolation).
- Validate all external inputs (e.g., user data, API requests) to prevent injection attacks.
- Avoid Thread.sleep(); use delay() instead.
- Use copy() carefully to avoid unintended side effects in immutable structures.
- Follow the official Kotlin Style Guide for consistency.
- Avoid hardcoding secrets (use environment variables).
- Leverage higher-order functions (e.g., map, filter) and avoid imperative loops where possible.
- Don't write meaningless comments that express in natural text what is already clear from the function names or code
- Do not write this kind-of comments:
- ```
  /**
   * Represents a document embedding with its metadata.
   *
   * @property id The unique identifier of the embedding
   * @property documentId The identifier of the document this embedding belongs to
   * @property content The text content that was embedded
   * @property embedding The vector representation of the content
   * @property metadata Additional metadata about the document (optional)
   * @property createdAt The timestamp when the embedding was created
  */
  ```
- Do not use mocking frameworks like mockito, mmock, power-mock, or anything similar
