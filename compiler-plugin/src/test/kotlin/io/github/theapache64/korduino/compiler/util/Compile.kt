package io.github.theapache64.korduino.compiler.util

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.core.ArgProcessor
import io.github.theapache64.korduino.compiler.core.Extension
import io.github.theapache64.korduino.compiler.core.Registrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File


fun compileArduino(sourceFiles: List<SourceFile>): JvmCompilationResult {
    return compile(sourceFiles, Arg.Platform.Target.ARDUINO)
}

fun compileStdCpp(sourceFiles: List<SourceFile>): JvmCompilationResult {
    return compile(sourceFiles, Arg.Platform.Target.STD_CPP)
}

private fun compile(
    sourceFiles: List<SourceFile>,
    target: Arg.Platform.Target
): JvmCompilationResult {
    return KotlinCompilation().apply {
        sources = sourceFiles
        compilerPluginRegistrars = listOf(Registrar())
        commandLineProcessors = listOf(ArgProcessor())
        inheritClassPath = true
        verbose = false
        messageOutputStream = System.out
        kotlincArguments += listOf(
            "-P", "plugin:com.tschuchort.compiletesting.maincommandlineprocessor:korduino:PLATFORM=${target.name}",
            "-P", "plugin:com.tschuchort.compiletesting.maincommandlineprocessor:korduino:BUILD_DIR=build",
        )
    }.compile()
}

@OptIn(ExperimentalCompilerApi::class)
fun JvmCompilationResult.readActualOutput(platform: Arg.Platform.Target): String {
    val pattern = "${Extension.CPP_MSG_PREFIX}'(.+)'".toRegex()
    val filePath = pattern.find(this.messages)?.groups[1]?.value ?: error("Couldn't find output file from messages")
    val outputDir = File(filePath).resolve(
        when(platform){
            Arg.Platform.Target.ARDUINO -> "pio/src/"
            Arg.Platform.Target.STD_CPP -> ""
        }
    )
    println("QuickTag: :readActualOutput: ${outputDir.absolutePath}")
    return outputDir.listFiles().filter { it.extension == "cpp" }[0].readText()
}