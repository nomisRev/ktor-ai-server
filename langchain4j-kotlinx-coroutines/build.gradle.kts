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
