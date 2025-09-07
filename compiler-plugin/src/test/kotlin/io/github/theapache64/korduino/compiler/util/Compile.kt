package io.github.theapache64.korduino.compiler.util

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.common.executeCommand
import io.github.theapache64.korduino.compiler.core.ArgProcessor
import io.github.theapache64.korduino.compiler.core.Extension
import io.github.theapache64.korduino.compiler.core.Registrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File


fun generateAndCompileArduinoSourceCode(sourceFiles: List<SourceFile>): JvmCompilationResult {
    return compileAndVerifyCompilability(
        sourceFiles,
        Arg.Platform.Target.ARDUINO,
        isCompile = true
    )
}

fun generateAndCompileCppSourceCode(sourceFiles: List<SourceFile>): JvmCompilationResult {
    return compileAndVerifyCompilability(
        sourceFiles,
        Arg.Platform.Target.STD_CPP,
        isCompile = true
    )
}

fun generateArduinoSourceCode(sourceFiles: List<SourceFile>): JvmCompilationResult {
    return compileAndVerifyCompilability(
        sourceFiles,
        Arg.Platform.Target.ARDUINO,
        isCompile = false
    )
}

fun generateCppSourceCode(sourceFiles: List<SourceFile>): JvmCompilationResult {
    return compileAndVerifyCompilability(
        sourceFiles,
        Arg.Platform.Target.STD_CPP,
        isCompile = false
    )
}

private fun compileAndVerifyCompilability(
    sourceFiles: List<SourceFile>,
    target: Arg.Platform.Target,
    isCompile: Boolean
): JvmCompilationResult {
    val result = KotlinCompilation().apply {
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

    if(isCompile){
        val projectDir = result.findProjectRootDir()
        when (target) {
            Arg.Platform.Target.ARDUINO -> {
                executeCommand(projectDir.resolve("pio"), arrayOf("pio", "run"))
            }

            Arg.Platform.Target.STD_CPP -> {
                executeCommand(projectDir.resolve("cpp"), arrayOf("g++", "*.cpp", "-o", "outs"))
            }
        }
    }
    return result
}

private fun JvmCompilationResult.findProjectRootDir(): File {
    val pattern = "${Extension.CPP_MSG_PREFIX}'(.+)'".toRegex()
    val filePath = pattern.find(this.messages)?.groups[1]?.value ?: error("Couldn't find output file from messages")
    return File(filePath)
}

@OptIn(ExperimentalCompilerApi::class)
fun JvmCompilationResult.readActualOutput(
    platform: Arg.Platform.Target,
): String {

    val outputDir = findProjectRootDir().resolve(
        when (platform) {
            Arg.Platform.Target.ARDUINO -> "pio/src/"
            Arg.Platform.Target.STD_CPP -> "cpp"
        }
    )
    println("QuickTag: :readActualOutput: ${outputDir.absolutePath}")
    val cppFile = outputDir.listFiles().filter { it.extension == "cpp" }[0]
    return cppFile.readText()
}

