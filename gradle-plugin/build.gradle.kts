plugins {
    kotlin("jvm") version "2.2.0"
    id("java-gradle-plugin")
}

group = "io.github.theapache64.korduino"
// [latest version - i promise!]
version = "0.0.1"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("korduinoGradlePlugin"){
            id = "io.github.theapache64.korduino.gradle"
            implementationClass = "io.github.theapache64.korduino.gradle.KorduinoPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}