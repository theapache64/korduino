package io.github.theapache64.korduino.compiler.core

import com.intellij.ide.plugins.PluginManagerCore.logger
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.common.ArgId
import io.github.theapache64.korduino.common.Baud
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class ArgProcessor : CommandLineProcessor {

    override val pluginId: String = "korduino"

    override val pluginOptions: Collection<CliOption> = ArgId.entries.map {
        when (it) {
            ArgId.ENABLED -> Arg.Enabled
            ArgId.PLATFORM -> Arg.Platform
            ArgId.BUILD_DIR -> Arg.BuildDir
            ArgId.BOARD -> Arg.Board
            ArgId.MONITOR_SPEED -> Arg.MonitorSpeed
            ArgId.UPLOAD_SPEED -> Arg.UploadSpeed
        }.toCliOption()
    }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        val argId = ArgId.valueOf(option.optionName)

        when (argId) {
            ArgId.ENABLED -> configuration.put(Arg.Enabled.key, value.toBoolean())
            ArgId.PLATFORM -> configuration.put(Arg.Platform.key, Arg.Platform.Target.valueOf(value))
            ArgId.BUILD_DIR -> configuration.put(Arg.BuildDir.key, value)
            ArgId.BOARD -> configuration.put(Arg.Board.key ,Arg.Board.Type.valueOf(value))
            ArgId.MONITOR_SPEED -> configuration.put(Arg.MonitorSpeed.key, Baud.valueOf(value))
            ArgId.UPLOAD_SPEED -> configuration.put(Arg.UploadSpeed.key, Baud.valueOf(value))
        }
    }
}


private fun Arg<*>.toCliOption(): CliOption {
    return CliOption(
        optionName = id.name, valueDescription = valueDescription, description = description, required = isRequired
    )
}

