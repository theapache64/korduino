plugins {
    kotlin("jvm") version "2.2.0"
}

group = "io.github.theapache64.korduino"
// [latest version - i promise!]
version = "0.0.1"

repositories {
    mavenCentral()
    // mavenLocal()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(kotlin("compiler"))
    implementation(project(":common"))

    // ### Testing ###
    testImplementation(kotlin("test"))
    testImplementation(project(":core"))

    // Kotlin Compiler for testing
    testImplementation("dev.zacsweers.kctfork:core:0.8.0")

    // Assertion
    testImplementation("com.github.theapache64:expekt:1.0.3")
}


tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi"
        )
    }
}