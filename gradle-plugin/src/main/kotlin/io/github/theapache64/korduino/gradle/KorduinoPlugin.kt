package io.github.theapache64.korduino.gradle

import io.github.theapache64.korduino.common.Arg
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

class KorduinoPlugin : Plugin<Project> {

    companion object {
        const val TASK_RUN_DESC = "Run your Kotlin code on connected microcontroller"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create("korduino", KorduinoExtension::class.java)
        val buildDir = extension.buildDir ?: project.layout.buildDirectory.asFile.get().also { buildDir ->
            extension.buildDir = buildDir
        }

        project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
            task.compilerOptions {
                freeCompilerArgs.addAll("-P", "plugin:korduino:BUILD_DIR=$buildDir")
                freeCompilerArgs.addAll(
                    "-P", "plugin:korduino:MODE=${
                        extension.target ?: error(
                            """
                    Korduino mode not set. <ARDUINO|STD_CPP>
                    
                    // example
                    korduino {
                        mode = "ARDUINO"
                    }
                """.trimIndent()
                        )
                    }"
                )
            }
        }

        project.tasks.register("run", RunKorduinoTask::class.java) { task ->
            task.group = "korduino"
            task.description = TASK_RUN_DESC
            task.extension = extension

            task.dependsOn("clean", "compileKotlin")
        }
    }
}

open class KorduinoExtension {
    var buildDir: File? = null
    var target: Arg.Platform.Target? = null
}

abstract class RunKorduinoTask : DefaultTask() {
    @get:Input
    @get:Optional
    lateinit var extension: KorduinoExtension

    @TaskAction
    fun execute() {
        when (extension.target) {
            Arg.Platform.Target.ARDUINO -> {
                try {
                    // Build and upload code
                    executeCommand("pio run --target upload")
                    // Start serial monitor
                    launchTerminal(
                        dir = extension.buildDir?.resolve("pio") ?: error("buildDir can't be null"),
                        command = "pio device monitor"
                    )
                } catch (e: Exception) {
                    logger.error("Task execution failed: ${e.message}", e)
                    throw e
                }
            }
            Arg.Platform.Target.STD_CPP -> {
                TODO()
            }

            null -> logger.error("Target can't be null")
        }

    }

    private fun launchTerminal(dir: File, @Suppress("SameParameterValue") command: String) {
        try {
            val processBuilder = when {
                System.getProperty("os.name").lowercase().contains("windows") -> {
                    // Windows: Use cmd.exe
                    ProcessBuilder(
                        "cmd.exe", "/c", "start", "cmd.exe", "/k", "cd /d \"${dir.absolutePath}\" && $command"
                    )
                }

                System.getProperty("os.name").lowercase().contains("mac") -> {
                    // macOS: Use Terminal.app with AppleScript
                    val script = """
                    tell application "Terminal"
                        activate
                        do script "cd '${dir.absolutePath}' && $command"
                    end tell
                """.trimIndent()
                    ProcessBuilder("osascript", "-e", script)
                }

                else -> {
                    // Linux/Unix: Try common terminal emulators
                    val terminals = listOf("gnome-terminal", "konsole", "xfce4-terminal", "xterm")
                    val availableTerminal = terminals.firstOrNull { isCommandAvailable(it) }

                    when (availableTerminal) {
                        "gnome-terminal" -> ProcessBuilder(
                            "gnome-terminal",
                            "--working-directory=${dir.absolutePath}",
                            "--",
                            "bash",
                            "-c",
                            "$command; exec bash"
                        )

                        "konsole" -> ProcessBuilder(
                            "konsole", "--workdir", dir.absolutePath, "-e", "bash", "-c", "$command; exec bash"
                        )

                        "xfce4-terminal" -> ProcessBuilder(
                            "xfce4-terminal",
                            "--working-directory=${dir.absolutePath}",
                            "--command=bash -c '$command; exec bash'"
                        )

                        "xterm" -> ProcessBuilder(
                            "xterm", "-e", "bash -c 'cd \"${dir.absolutePath}\" && $command; exec bash'"
                        )

                        else -> throw RuntimeException("No supported terminal emulator found")
                    }
                }
            }

            processBuilder.start()
        } catch (e: Exception) {
            println("Failed to launch terminal: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun isCommandAvailable(command: String): Boolean {
        return try {
            ProcessBuilder("which", command).start().waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    @Suppress("SameParameterValue")
    private fun executeCommand(
        command: String
    ) {
        val process = ProcessBuilder(*command.split(" ").toTypedArray()).directory(
            extension.buildDir?.resolve("pio") ?: error("buildDir can't be null")
        ).redirectOutput(ProcessBuilder.Redirect.PIPE).redirectError(ProcessBuilder.Redirect.PIPE).start()


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