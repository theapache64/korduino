plugins {
    kotlin("jvm")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("compiler"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}