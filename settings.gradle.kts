enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "KtorProject"
include("jakarta-validation")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
