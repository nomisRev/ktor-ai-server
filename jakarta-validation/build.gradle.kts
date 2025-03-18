plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.assert)
}

dependencies {
    api(libs.bundles.jakarta.validation)
    implementation(libs.bundles.ktor.server)
    implementation("org.apache.bval:bval-jsr:3.0.2")
    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.bundles.testing)
}
