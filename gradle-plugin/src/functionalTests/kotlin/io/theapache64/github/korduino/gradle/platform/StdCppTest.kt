@file:Suppress("FunctionName")

package io.theapache64.github.korduino.gradle.platform

import com.github.theapache64.expekt.should
import io.github.theapache64.korduino.gradle.KorduinoPlugin
import io.theapache64.github.korduino.gradle.createProject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class StdCppTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `Run single cpp file`() {
        val project = testProjectDir.createProject(
            """
           plugins {
                kotlin("jvm")
                id("io.github.theapache64.korduino.gradle")
           }
           
           repositories {
                mavenCentral()
           }
           korduino {
               platform = "STD_CPP"
           }
        """.trimIndent()
        )

        // Create kotlin file first
        val kotlinContent = """
            fun main(){
                println("Hello Kotlin")
            }
        """.trimIndent()

        // Creating source dir first
        testProjectDir.newFolder("src/main/kotlin")
        testProjectDir.newFile("src/main/kotlin/Main.kt")
            .writeText(kotlinContent)

        val task = project.withArguments("run").build()
        task.output.should.contain("Hello Kotlin")
    }
}