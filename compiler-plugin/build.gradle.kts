plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(libs.kotlin.compiler)
    implementation(project(":common"))

    // ### Testing ###
    testImplementation(libs.kotlin.test)
    testImplementation(project(":core"))

    // JUnit 5 dependencies
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Kotlin Compiler for testing
    testImplementation(libs.kctfork.core)

    // Assertion
    testImplementation(libs.expekt)
}

tasks.test {
    useJUnitPlatform()

    // Ensure dependencies are published to maven local before running tests
    dependsOn(":common:publishToMavenLocal")
}

kotlin {
    jvmToolchain((property("jvmToolchain") as String).toInt())

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi"
        )
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
