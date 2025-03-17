enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ktor-sample"
include("jakarta-validation")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
