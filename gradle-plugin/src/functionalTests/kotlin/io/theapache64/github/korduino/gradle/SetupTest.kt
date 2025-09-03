package io.theapache64.github.korduino.gradle

import com.github.theapache64.expekt.should
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.gradle.KorduinoPlugin
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SetupTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `Check test setup`() {
        for (platform in Arg.Platform.Target.entries) {
            val project = testProjectDir.createProject(
                """
           plugins {
                kotlin("jvm")
                id("io.github.theapache64.korduino.gradle")
           }
           
           korduino {
               platform = "${platform.name}"
           }
        """.trimIndent()
            )
            val task = project.withArguments("tasks").build()
            task.output.should.contain(KorduinoPlugin.TASK_RUN_DESC)
        }
    }

}