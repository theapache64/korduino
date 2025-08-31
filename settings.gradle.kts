pluginManagement {
    includeBuild("gradle-plugin")
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "korduino"
include("core")
include("compiler-plugin")
include("sample")