package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.compiler.Arg
import io.github.theapache64.korduino.compiler.ArgId
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class ArgProcessor : CommandLineProcessor {

    override val pluginId: String = "korduino"

    override val pluginOptions: Collection<CliOption> =
        ArgId.entries.map {
            when (it) {
                ArgId.ENABLED -> Arg.Enabled.toCliOption()
                ArgId.MODE -> Arg.Mode.toCliOption()
                ArgId.BUILD_DIR -> Arg.BuildDir.toCliOption()
            }
        }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        val argId = ArgId.valueOf(option.optionName)
        when (argId) {
            ArgId.ENABLED -> configuration.put(Arg.Enabled.key, value.toBoolean())
            ArgId.MODE -> configuration.put(Arg.Mode.key, Arg.Mode.Platform.valueOf(value))
            ArgId.BUILD_DIR -> configuration.put(Arg.BuildDir.key, value)
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

