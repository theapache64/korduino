plugins {
    kotlin("jvm") version "2.2.0"
    id("java-gradle-plugin")
}

group = "io.github.theapache64.korduino"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("compiler"))
    implementation(project(":common"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}