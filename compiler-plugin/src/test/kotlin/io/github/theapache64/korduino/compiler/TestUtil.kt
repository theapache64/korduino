package io.github.theapache64.korduino.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.utils.indexOfFirst
import org.junit.jupiter.api.Assertions
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.InvocationTargetException

fun assertFunction(javaCode: String, functionStatement: String, expectedFunction: String) {
    Assertions.assertEquals(expectedFunction, fetchMethodByPrefix(javaCode, functionStatement))
}

fun fetchMethodByPrefix(classText: String, methodSignaturePrefix: String): String {
    val classLines = classText.split("\n")
    val methodSignaturePredicate: (String) -> Boolean = { line -> line.contains(methodSignaturePrefix) }
    val methodFirstLineIndex = classLines.indexOfFirst(methodSignaturePredicate)

    check(methodFirstLineIndex != -1) {
        "Method with prefix '$methodSignaturePrefix' not found within class:\n$classText"
    }

    val multiplePrefixMatches = classLines
        .indexOfFirst(methodFirstLineIndex + 1, methodSignaturePredicate)
        .let { index -> index != -1 }

    check(!multiplePrefixMatches) {
        "Multiple methods with prefix '$methodSignaturePrefix' found within class:\n$classText"
    }

    val indentationSize = classLines[methodFirstLineIndex].takeWhile { it == ' ' }.length

    var curleyBraceCount = 1
    var currentLineIndex: Int = methodFirstLineIndex + 1

    while (curleyBraceCount != 0 && currentLineIndex < classLines.lastIndex) {
        if (classLines[currentLineIndex].contains("{")) {
            curleyBraceCount++
        }
        if (classLines[currentLineIndex].contains("}")) {
            curleyBraceCount--
        }
        currentLineIndex++
    }

    return classLines
        .subList(methodFirstLineIndex, currentLineIndex)
        .joinToString("\n") { it.substring(indentationSize) }
}



@OptIn(ExperimentalCompilerApi::class)
fun compile(
    sourceFiles: List<SourceFile>,
): JvmCompilationResult {
    return KotlinCompilation().apply {
        sources = sourceFiles
        compilerPluginRegistrars = listOf(Registrar())
        inheritClassPath = true
        verbose = false
        messageOutputStream = System.out
    }.compile()
}


@OptIn(ExperimentalCompilerApi::class)
fun invokeMain(result: JvmCompilationResult, className: String): String {
    val oldOut = System.out
    try {
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer, false, "UTF-8"))

        try {
            val kClazz = result.classLoader.loadClass(className)
            val main = kClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }
            main.invoke(null)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }

        return buffer.toString("UTF-8")
    } finally {
        System.setOut(oldOut)
    }
}
