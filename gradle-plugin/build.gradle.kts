plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("maven-publish")
}


repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

gradlePlugin {
    plugins {
        create("korduinoGradlePlugin") {
            id = "io.github.theapache64.korduino.gradle"
            implementationClass = "io.github.theapache64.korduino.gradle.KorduinoPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(project(":common"))

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.github.theapache64:expekt:1.0.3")
}

tasks.test {
    useJUnitPlatform()

    // Ensure dependencies are published to maven local before running tests
    dependsOn(":common:publishToMavenLocal")
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    create("functionalTests") {
        kotlin.srcDir(file("src/functionalTests/kotlin"))
        resources.srcDir(file("src/functionalTests/resources"))
        compileClasspath += sourceSets["main"].output + configurations["testCompileClasspath"]
        runtimeClasspath += output + compileClasspath + configurations["testRuntimeClasspath"]
    }
}

val functionalTest = tasks.register<Test>("functionalTest") {
    description = "Runs the functional tests"
    group = "verification"
    testClassesDirs = sourceSets["functionalTests"].output.classesDirs
    classpath = sourceSets["functionalTests"].runtimeClasspath

    // Ensure all publishable modules are published to maven local before running tests
    dependsOn(
        ":common:publishToMavenLocal",
        ":compiler-plugin:publishToMavenLocal",
        ":gradle-plugin:publishToMavenLocal"
    )
}

tasks.named("test") {
    dependsOn(functionalTest)
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