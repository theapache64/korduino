plugins {
    kotlin("jvm") version "2.2.0"
    id("io.github.theapache64.korduino.gradle")
}

group = "io.github.theapache64.korduino"
// [latest version - i promise!]
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    kotlinCompilerPluginClasspath(project(":compiler-plugin"))
    testImplementation(kotlin("test"))
}

korduino {
    mode = "ARDUINO"
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}