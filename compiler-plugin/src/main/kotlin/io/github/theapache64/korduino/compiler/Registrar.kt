package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.File


@OptIn(ExperimentalCompilerApi::class)
class Registrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        var outputFile = configuration.get(PluginKeys.OUTPUT_FILE)

        if (outputFile == null) {
            outputFile = "/Users/theapache64/Documents/PlatformIO/Projects/hello-pio/src/main.cpp"
        }

        IrGenerationExtension.registerExtension(
            Extension(messageCollector, outputFile)
        )
    }

}