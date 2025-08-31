plugins {
    kotlin("jvm") version "2.2.0"
    id("java-gradle-plugin")
}

group = "io.github.theapache64.korduino"
version = "0.0.1"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    testImplementation(kotlin("test"))

    testImplementation(project(":core"))
    testImplementation("dev.zacsweers.kctfork:core:0.8.0")
    testImplementation("com.github.theapache64:expekt:1.0.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
