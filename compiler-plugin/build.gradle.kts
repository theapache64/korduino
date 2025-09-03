plugins {
    kotlin("jvm")
}

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