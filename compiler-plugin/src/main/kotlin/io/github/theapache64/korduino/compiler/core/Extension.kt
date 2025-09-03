package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.common.Arg
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.backend.js.utils.nameWithoutExtension
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

class Extension(
    private val messageCollector: MessageCollector,
    private val target: Arg.Platform.Target,
    private val buildDir: String,
    private  val targetDirGenerator: TargetDirGenerator
) : IrGenerationExtension {

    companion object {
        const val CPP_MSG_PREFIX = "CPP code generated at: "
    }

    private val tempDir = Path("$buildDir${File.separator}cpp").also { tempDir ->
        if (!tempDir.exists()) {
            tempDir.createDirectories()
        }
    }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val visitor = Visitor(target)
        moduleFragment.accept(visitor, null)
        val files = mutableListOf<Path>().apply {
            for (moduleFile in moduleFragment.files) {
                val cppCode = visitor.generateCode() // TODO: Support multiple files
                val file = tempDir.resolve("${moduleFile.nameWithoutExtension}.cpp").apply {
                    writeText(cppCode)
                }
                add(file)
            }
        }
        val srcDir = targetDirGenerator.create(files, buildDir)
        messageCollector.report(
            CompilerMessageSeverity.INFO, "$CPP_MSG_PREFIX'${srcDir.absolutePathString()}'"
        )
    }
}