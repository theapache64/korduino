package io.github.theapache64.korduino.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.compiler.core.ArgProcessor
import io.github.theapache64.korduino.compiler.core.Registrar


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