@file:Suppress("FunctionName")

package io.theapache64.github.korduino.gradle

import com.github.theapache64.expekt.should
import io.github.theapache64.korduino.gradle.KorduinoPlugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ArduinoTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `Check test setup`() {
        val project = createProject(
            """
           plugins {
                kotlin("jvm")
                id("io.github.theapache64.korduino.gradle")
           }
           
           korduino {
               mode = "ARDUINO"
           }
        """.trimIndent()
        )
        val task = project.withArguments("tasks").build()
        task.output.should.contain(KorduinoPlugin.TASK_RUN_DESC)
    }

    private fun createProject(gradleFileContent: String): GradleRunner {
        val buildGradleFile = testProjectDir.newFile("build.gradle.kts")

        // Adding plugin
        buildGradleFile.appendText(gradleFileContent)

        return GradleRunner.create().withPluginClasspath().withProjectDir(testProjectDir.root)
            .withTestKitDir(testProjectDir.newFolder())
    }
}