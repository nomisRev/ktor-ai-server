# Running the WASM Application

This document provides instructions on how to run the WebAssembly (WASM) version of the application.

## Prerequisites

- JDK 11 or higher
- Gradle 7.0 or higher

## Running the WASM App in Development Mode

To run the WASM application in development mode with hot-reload:

```bash
./gradlew wasmJsBrowserDevelopmentRun
```

This command will:
1. Compile the Kotlin code to WASM
2. Start a webpack dev server
3. Open a browser window with your application

The development server typically runs at http://localhost:8080 by default.

## Building for Production

To build a production-ready WASM bundle:

```bash
./gradlew wasmJsBrowserProductionWebpack
```

This will create optimized WASM files in the `composeApp/build/dist/wasmJs/productionExecutable` directory.

To run the production build with a local server:

```bash
./gradlew wasmJsBrowserProductionRun
```

## Project Structure

The WASM-specific code is located in:
- `composeApp/src/wasmJsMain/` - Contains WASM-specific implementations

The main entry point for the WASM application is:
- `composeApp/src/wasmJsMain/kotlin/org/jetbrains/main.kt`

## Troubleshooting

If you encounter any issues:

1. Make sure you have the latest Kotlin plugin installed
2. Try cleaning the build with `./gradlew clean` before rebuilding
3. Check that your browser supports WebAssembly
4. If you're using Chrome, try opening the developer console (F12) to see any error messages