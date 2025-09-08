package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.common.executeCommand
import kotlin.io.path.*

class CppFormatter() {
    fun format(code: String): String {
        val cppFile = createTempFile("temp_file_for_format", ".cpp")
        cppFile.writeText(code)

        // Format
        executeCommand(
            cppFile.parent.toFile(),
            arrayOf(
                "clang-format",
                "-i",
                cppFile.absolutePathString(),
                "--style",
                "{BasedOnStyle: Chromium, IndentWidth: 4}"
            )
        )
        return cppFile.readText().also {
            cppFile.deleteIfExists()
        }
    }

    fun moveFunction(function: CppFunction, fullCode: StringBuilder, moveToLineNo: Int): String {
        var tempFullCode = fullCode.toString()
        val fullCodeLines = tempFullCode.lines()
        val extractedFunction = fullCodeLines.subList(function.startLineNo, function.endLineNo).joinToString("\n")
        tempFullCode = tempFullCode.replace(extractedFunction, "")
        val fullCodeBuilder = tempFullCode.lines().toMutableList()
        fullCodeBuilder.add(moveToLineNo, extractedFunction)
        tempFullCode = fullCodeBuilder.joinToString("\n")
        return tempFullCode
    }
}