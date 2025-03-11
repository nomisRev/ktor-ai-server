
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.assert)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

@Suppress("OPT_IN_USAGE")
powerAssert {
    functions = listOf("kotlin.test.assertEquals")
}

dependencies {
    implementation(libs.bundles.ktor.core)
    implementation(libs.bundles.ktor.auth)
    implementation(libs.bundles.exposed)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.datetime)
    implementation(libs.bouncycastle)
    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.bundles.testing)
}
