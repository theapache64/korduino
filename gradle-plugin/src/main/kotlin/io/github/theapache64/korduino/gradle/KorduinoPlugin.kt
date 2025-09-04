package io.github.theapache64.korduino.gradle

import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.common.executeCommand
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

        project.dependencies.add(
            "kotlinCompilerPluginClasspath",
            "io.github.theapache64.korduino:compiler-plugin:0.0.1" // TODO: The version and group name should come from root kts file
        )


        project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
            task.compilerOptions {
                val buildDir = extension.buildDir ?: project.layout.buildDirectory.asFile.get().also { buildDir ->
                    extension.buildDir = buildDir
                }

                freeCompilerArgs.addAll("-P", "plugin:korduino:BUILD_DIR=$buildDir")
                freeCompilerArgs.addAll(
                    "-P", "plugin:korduino:PLATFORM=${
                        extension.platform ?: error(
                            """
                    Korduino mode not set. <ARDUINO|STD_CPP>
                    
                    // example
                    korduino {
                        platform = "ARDUINO"
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
    var platform: String? = null
}

abstract class RunKorduinoTask : DefaultTask() {
    @get:Input
    @get:Optional
    lateinit var extension: KorduinoExtension

    @TaskAction
    fun execute() {

        val platform = Arg.Platform.Target.valueOf(extension.platform ?: error("platform can't be null"))
        when (platform) {
            Arg.Platform.Target.ARDUINO -> {
                try {
                    // Build and upload code
                    executeCommand(
                        extension.buildDir?.resolve("pio") ?: error("buildDir can't be null"),
                        arrayOf("pio", "run", "--target", "upload")
                    )
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
                try {
                    val cppDir = extension.buildDir?.resolve("cpp")
                    val cppFile = cppDir?.listFiles()?.find { it.extension == "cpp" }
                        ?: error("Couldn't find a cpp file in '${extension.buildDir?.absolutePath}'")
                    println("QuickTag: RunKorduinoTask:execute: cppFile: ${cppFile.absolutePath} -> ${cppFile.exists()}")
                    executeCommand(cppDir, arrayOf("g++", cppFile.absolutePath, "-o", "outs"))
                    executeCommand(cppDir, arrayOf("./outs"))
                } catch (e: Exception) {
                    logger.error("Task execution failed: ${e.message}", e)
                    throw e
                }
            }
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
}

