package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.io.File
import java.io.IOException

class Extension(
    private val messageCollector: MessageCollector,
    private val outputFilePath: String
) : IrGenerationExtension {

    companion object {
        const val CPP_MSG_PREFIX = "C\\+\\+ code generated at: "
    }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val visitor = Visitor()
        moduleFragment.accept(visitor, null)
        val cppCode = visitor.generateCode()

        val file = File(outputFilePath)
        file.writeText(cppCode)
        messageCollector.report(
            org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.INFO,
            "$CPP_MSG_PREFIX '${file.absolutePath}'"
        )
    }
}