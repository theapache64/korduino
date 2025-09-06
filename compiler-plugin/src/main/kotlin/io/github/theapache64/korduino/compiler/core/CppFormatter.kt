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

    fun moveFunction(function: CppFunction, _fullCode: StringBuilder, moveToLineNo: Int): String {
        var fullCode = _fullCode.toString()
        val fullCodeLines = fullCode.lines()
        val extractedFunction = fullCodeLines.subList(function.startLineNo, function.endLineNo).joinToString("\n")
        fullCode = fullCode.replace(extractedFunction, "")
        val fullCodeBuilder= fullCode.lines().toMutableList()
        fullCodeBuilder.add(moveToLineNo, extractedFunction)
        return fullCodeBuilder.joinToString("\n")
    }
}