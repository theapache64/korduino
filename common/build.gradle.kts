plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.compiler)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain((property("jvmToolchain") as String).toInt())
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
