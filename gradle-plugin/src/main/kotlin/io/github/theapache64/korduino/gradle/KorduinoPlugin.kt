package io.github.theapache64.korduino.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.bufferedReader
import kotlin.io.useLines
import kotlin.jvm.java
import kotlin.sequences.forEach
import kotlin.text.trimIndent

class KorduinoPlugin : Plugin<Project> {
    override fun apply(target: Project) {

        val extension = target.extensions.create("korduino", KorduinoExtension::class.java)

        target.tasks.register("run", RunKorduinoTask::class.java) { task ->
            task.group = "korduino"
            task.description = "Clean, compile Kotlin, copy files, and run PlatformIO commands"
            task.extension = extension

            task.dependsOn("clean", "compileKotlin")
        }
    }
}

open class KorduinoExtension {
    var sourceFile: String = ""
    var destinationFile: String = ""
    var workingDirectory: String = ""
}

abstract class RunKorduinoTask : DefaultTask() {
    @get:Input
    @get:Optional
    lateinit var extension: KorduinoExtension

    @TaskAction
    fun execute() {
        try {
            executePioUpload()
            executePioMonitor()
        } catch (e: Exception) {
            logger.error("Task execution failed: ${e.message}", e)
            throw e
        }
    }

    private fun copyFile() {
        val sourceFile = File(extension.sourceFile)
        val destinationFile = File(extension.destinationFile)

        if (!sourceFile.exists()) {
            throw kotlin.IllegalArgumentException("Source file does not exist: ${extension.sourceFile}")
        }


        destinationFile.parentFile?.mkdirs()

        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        logger.info("File copied from ${extension.sourceFile} to ${extension.destinationFile}")
    }

    private fun executePioUpload() {
        logger.info("Executing: pio run --target upload")


        val process = ProcessBuilder("pio", "run", "--target", "upload")
            .directory(File("/Users/theapache64/Documents/PlatformIO/Projects/hello-pio"))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()



        val outputThread = Thread {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    println("PIO: $line")
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
            logger.error("PlatformIO upload failed. Common solutions:")
            logger.error("1. Check if the device is connected and recognized")
            logger.error("2. Verify platformio.ini configuration")
            logger.error("3. Check if another application is using the serial port")
            logger.error("4. Try running 'pio device list' to see available devices")
            logger.error("5. Ensure the correct board and platform are specified")
            throw kotlin.RuntimeException("pio run --target upload failed with exit code: $exitCode")
        }

        logger.info("PIO upload completed successfully")
    }

    private fun executePioMonitor() {
        logger.info("Executing: pio device monitor")

        // TODO: Support other platforms
        val script = $$"""
                        tell application "Terminal"
                            do script "cd '/Users/theapache64/Documents/PlatformIO/Projects/hello-pio' && export PATH=/opt/homebrew/bin:/usr/local/bin:$PATH && pio device monitor"
                            activate
                        end tell
                    """.trimIndent()
        val processBuilder = ProcessBuilder("osascript", "-e", script)

        val process = processBuilder.start()


        Thread.sleep(1000)

        println("✅ Terminal opened with PlatformIO Device Monitor")
        println("💡 The monitor is running in a separate terminal window")

    }
}