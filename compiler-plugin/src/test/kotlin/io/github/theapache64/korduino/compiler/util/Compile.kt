package io.github.theapache64.korduino.compiler.util

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.compiler.Arg
import io.github.theapache64.korduino.compiler.core.ArgProcessor
import io.github.theapache64.korduino.compiler.core.Extension
import io.github.theapache64.korduino.compiler.core.Registrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File


fun compileArduino(sourceFiles: List<SourceFile>): JvmCompilationResult {
    return compile(sourceFiles, Arg.Mode.Platform.ARDUINO)
}

fun compileStdCpp(sourceFiles: List<SourceFile>): JvmCompilationResult {
    return compile(sourceFiles, Arg.Mode.Platform.STD_CPP)
}

private fun compile(
    sourceFiles: List<SourceFile>,
    platform: Arg.Mode.Platform
): JvmCompilationResult {
    return KotlinCompilation().apply {
        sources = sourceFiles
        compilerPluginRegistrars = listOf(Registrar())
        commandLineProcessors = listOf(ArgProcessor())
        inheritClassPath = true
        verbose = false
        messageOutputStream = System.out
        kotlincArguments += listOf(
            "-P", "plugin:com.tschuchort.compiletesting.maincommandlineprocessor:korduino:MODE=${platform.name}",
        )
    }.compile()
}

@OptIn(ExperimentalCompilerApi::class)
fun JvmCompilationResult.readActualOutput(): String {
    val pattern = "${Extension.CPP_MSG_PREFIX}'(.+)'".toRegex()
    val filePath = pattern.find(this.messages)?.groups[1]?.value ?: error("Couldn't find output file from messages")
    val outputDir = File(filePath).resolve("pio/src/")
    return outputDir.listFiles().filter { it.extension == "cpp" }[0].readText()
}