package io.github.theapache64.korduino.common

import java.io.File

fun executeCommand(
    directory: File,
    command: Array<String>,
) {
    val process = ProcessBuilder(*command).directory(
        directory
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
                println("ERROR: $line")
            }
        }
    }

    outputThread.start()
    errorThread.start()

    val exitCode = process.waitFor()
    outputThread.join()
    errorThread.join()

    if (exitCode != 0) {
        throw kotlin.RuntimeException("`${command.joinToString(" ")}` failed with exit code: $exitCode")
    }
}