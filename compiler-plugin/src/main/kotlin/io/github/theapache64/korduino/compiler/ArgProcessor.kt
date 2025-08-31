package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class ArgProcessor : CommandLineProcessor {

    override val pluginId: String = "korduino"

    override val pluginOptions: Collection<CliOption> =
        ArgId.entries.map {
            when (it) {
                ArgId.ENABLED -> Arg.Enabled.toCliOption()
                ArgId.MODE -> Arg.Mode.toCliOption()
            }
        }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        val argId = ArgId.valueOf(option.optionName)
        when (argId) {
            ArgId.ENABLED -> configuration.put(Arg.Enabled.key, value.toBoolean())
            ArgId.MODE -> configuration.put(Arg.Mode.key, Arg.Mode.Platform.valueOf(value))
        }
    }
}


private fun Arg<*>.toCliOption(): CliOption {
    return CliOption(
        optionName = id.name,
        valueDescription = valueDescription,
        description = description,
        required = isRequired
    )
}

