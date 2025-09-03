pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "korduino"

include("common")
include("core")
include("compiler-plugin")
include("gradle-plugin")
