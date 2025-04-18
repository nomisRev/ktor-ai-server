import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                add(rootDirPath)
                                add(projectDirPath)
                            }
                    }
            }
            @OptIn(ExperimentalDistributionDsl::class)
            distribution { outputDirectory = file("$rootDir/backend/src/main/resources/web") }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.cio)
        }
    }
}

tasks {
    register("buildDevWebsite") {
        group = "kotlin browser"
        description = "Builds the website in development mode"
        dependsOn("wasmJsBrowserDevelopmentWebpack")
        dependsOn("wasmJsBrowserDevelopmentExecutableDistribution")
    }

    register("buildProdWebsite") {
        group = "kotlin browser"
        description = "Builds the website in production mode"
        dependsOn("wasmJsBrowserProductionWebpack")
        dependsOn("wasmJsBrowserDistribution")
    }
}
