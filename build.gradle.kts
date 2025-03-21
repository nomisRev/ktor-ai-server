import io.ktor.plugin.features.DockerImageRegistry.Companion.dockerHub
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.assert)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "org.jetbrains"
version = "0.0.1"

application.mainClass = "io.ktor.server.netty.EngineMain"

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.exposed)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.datetime)
    implementation(libs.bouncycastle)
    implementation(libs.bundles.flyway)
    implementation(libs.bundles.langchain4j)
    implementation(libs.micrometer.registry.prometheus)

    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.bundles.testing)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        javaParameters = true
    }
}

ktor {
    development = System.getenv("CI") != null
    docker {
        localImageName = "ktor-ai-example"
        imageTag = project.version.toString()
        externalRegistry = dockerHub(
            appName = provider { project.name },
            username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
            password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
        )
    }
    fatJar {
        allowZip64 = true
        archiveFileName.set(project.name)
    }
}
