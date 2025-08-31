package io.github.theapache64.korduino.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import io.github.theapache64.korduino.compiler.core.Extension
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
fun JvmCompilationResult.readActualOutput(): String {
    val pattern = "${Extension.CPP_MSG_PREFIX}'(.+)'".toRegex()
    val filePath = pattern.find(this.messages)?.groups[1]?.value ?: error("Couldn't find output file from messages")
    return File(filePath).readText()
}
