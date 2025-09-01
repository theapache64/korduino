package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.compiler.Arg
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.backend.js.utils.nameWithoutExtension
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class Extension(
    private val messageCollector: MessageCollector, private val platform: Arg.Mode.Platform
) : IrGenerationExtension {

    companion object {
        const val CPP_MSG_PREFIX = "CPP code generated at: "
    }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val visitor = Visitor(platform)
        moduleFragment.accept(visitor, null)
        val files = mutableListOf<Path>()
        val fileName = moduleFragment.files[0].nameWithoutExtension
        val cppCode = visitor.generateCode()
        val file = createTempFile(fileName, suffix = ".cpp").apply {
            writeText(cppCode)
        }
        files.add(file) // TODO: Manage multiple files
        val srcDir = Pio.create(files)
        messageCollector.report(
            CompilerMessageSeverity.INFO, "$CPP_MSG_PREFIX'${srcDir.absolutePathString()}'"
        )
    }
}