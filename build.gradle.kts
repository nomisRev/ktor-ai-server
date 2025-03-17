import io.ktor.plugin.features.DockerImageRegistry.Companion.dockerHub

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.assert)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "org.jetbrains"
version = "0.0.1"

application.mainClass = "io.ktor.server.netty.EngineMain"

@Suppress("OPT_IN_USAGE")
powerAssert {
    functions = listOf("kotlin.assert")
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.exposed)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.datetime)
    implementation(libs.bouncycastle)
    implementation(libs.bundles.flyway)
    implementation(libs.bundles.langchain4j)
    implementation(projects.jakartaValidation)

    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.bundles.testing)
}

ktor {
    docker {
        localImageName = project.name
        imageTag = project.version.toString()
        externalRegistry = dockerHub(
            appName = provider { project.name },
            username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
            password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
        )
    }
    development = System.getenv("CI") != null
    fatJar {
        allowZip64 = true
        archiveFileName.set(project.name)
    }
}
