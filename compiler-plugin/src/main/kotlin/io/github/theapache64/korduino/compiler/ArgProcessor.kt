package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

enum class Target {
    ARDUINO,
    CPP
}

object PluginKeys {
    val ENABLED = CompilerConfigurationKey<Boolean>("enabled")
    val OUTPUT_FILE = CompilerConfigurationKey<String>("outputFile")
    val TARGET = CompilerConfigurationKey<Target>("target") // TODO:
}


@OptIn(ExperimentalCompilerApi::class)
class ArgProcessor : CommandLineProcessor {

    override val pluginId: String = "korduino"

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption("enabled", "<true|false>", "Enable the plugin", required = true),
        CliOption("outputFile", "<path>", "Output file path", required = true),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {

        when (option.optionName) {
            "enabled" -> configuration.put(PluginKeys.ENABLED, value.toBoolean())
            "outputFile" -> configuration.put(PluginKeys.OUTPUT_FILE, value)
        }
    }
}
