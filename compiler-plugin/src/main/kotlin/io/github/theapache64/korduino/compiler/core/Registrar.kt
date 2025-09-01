package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.compiler.Arg
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration


@OptIn(ExperimentalCompilerApi::class)
class Registrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val platform = configuration.get(Arg.Mode.key, Arg.Mode.Platform.ARDUINO)

        IrGenerationExtension.registerExtension(
            extension = Extension(messageCollector, platform)
        )
    }

}