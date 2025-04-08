import io.ktor.plugin.features.DockerImageRegistry.Companion.dockerHub
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.assert)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "org.jetbrains"
version = "0.0.1"

application.mainClass = "io.ktor.server.netty.EngineMain"

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.bundles.exposed)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.datetime)
    implementation(libs.bouncycastle)
    implementation(libs.bundles.flyway)
    implementation(libs.bundles.langchain4j)
    implementation(libs.micrometer.registry.prometheus)
    implementation(projects.langchain4jKotlinxCoroutines)

    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.bundles.testing)
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

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            // Needed for LangChain4J reflection tricks
            // Maintains the parameter names instead of replacing with $0, $1, etc.
            javaParameters = true
        }
    }

    val cleanWebsite = create<Delete>("cleanWebsite") {
        group = "build"
        delete(
            fileTree("${project.projectDir}/src/main/resources/web")
        )
    }

    findByName("clean")?.dependsOn(cleanWebsite)
    findByName("run")?.dependsOn(":composeApp:buildDevWebsite")
}
