import io.ktor.plugin.features.DockerImageRegistry.Companion.dockerHub
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.assert)
}

group = "org.jetbrains"
version = "0.0.1"

dependencies {
    implementation(libs.bundles.langchain4j)
    implementation(libs.kotlinx.coroutines)
    testImplementation(libs.bundles.testing)
}
