package io.theapache64.github.korduino.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder

fun TemporaryFolder.createProject(gradleFileContent: String): GradleRunner {
    val buildGradleFile = newFile("build.gradle.kts")

    // Adding plugin
    buildGradleFile.appendText(gradleFileContent)

    return GradleRunner.create()
        .withPluginClasspath()
        .withProjectDir(root)
        .withTestKitDir(newFolder())
}