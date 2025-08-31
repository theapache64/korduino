plugins {
    kotlin("jvm") version "2.2.0"
}

group = "io.github.theapache64.korduino"
// [latest version - i promise!]
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}