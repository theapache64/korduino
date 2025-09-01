package io.github.theapache64.korduino.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

class KorduinoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("korduino", KorduinoExtension::class.java)
        val buildDir = extension.buildDir ?: project.layout.buildDirectory.asFile.get().also { buildDir ->
            extension.buildDir = buildDir
        }

        project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
            task.compilerOptions {
                freeCompilerArgs.addAll("-P", "plugin:korduino:BUILD_DIR=$buildDir")
            }
        }

        project.tasks.register("run", RunKorduinoTask::class.java) { task ->
            task.group = "korduino"
            task.description = "Clean, compile Kotlin, copy files, and run PlatformIO commands"
            task.extension = extension

            task.dependsOn("clean", "compileKotlin")
        }
    }
}

open class KorduinoExtension {
    var buildDir: File? = null
}

abstract class RunKorduinoTask : DefaultTask() {
    @get:Input
    @get:Optional
    lateinit var extension: KorduinoExtension

    @TaskAction
    fun execute() {
        try {
            // Build and upload code
            executeCommand("pio run --target upload")
            // Start serial monitor
            executeCommand("script -q /dev/null pio device monitor --raw")
        } catch (e: Exception) {
            logger.error("Task execution failed: ${e.message}", e)
            throw e
        }
    }


    private fun executeCommand(
        command: String
    ) {
        val process = ProcessBuilder(*command.split(" ").toTypedArray())
            .directory(extension.buildDir?.resolve("pio") ?: error("buildDir can't be null"))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()


        val outputThread = Thread {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    println(line)
                }
            }
        }

        val errorThread = Thread {
            process.errorStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    println("PIO ERROR: $line")
                }
            }
        }

        outputThread.start()
        errorThread.start()

        val exitCode = process.waitFor()
        outputThread.join()
        errorThread.join()

        if (exitCode != 0) {
            logger.error("PlatformIO command failed")
            throw kotlin.RuntimeException("`$command` failed with exit code: $exitCode")
        }

        logger.info("PIO upload completed successfully")
    }
}