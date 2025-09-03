package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.common.Hello
import io.github.theapache64.korduino.compiler.Arg
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

class Registrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        println("QuickTag: Registrar:registerExtensions: ${Hello.say()}")
        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val platform = configuration.get(Arg.Mode.key, Arg.Mode.Platform.ARDUINO)
        val buildDir = configuration.get(Arg.BuildDir.key) ?: error("buildDir can't be null")
        IrGenerationExtension.registerExtension(
            extension = Extension(messageCollector, platform, buildDir)
        )
    }

}