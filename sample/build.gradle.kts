plugins {
    kotlin("jvm")
}

group = "io.github.theapache64.korduino"
// [latest version - i promise!]
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}

/*korduino {
    mode = "ARDUINO"
}*/

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}