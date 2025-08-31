package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.config.CompilerConfigurationKey

enum class ArgId {
    ENABLED,
    MODE
}

sealed class Arg<T>(
    val id : ArgId,
    val key: CompilerConfigurationKey<T>,
    val valueDescription: String,
    val description: String,
    val isRequired: Boolean
) {
    object Enabled : Arg<Boolean>(
        id = ArgId.ENABLED,
        key = CompilerConfigurationKey(ArgId.ENABLED.name),
        valueDescription = "<true|false>",
        description = "Enable the plugin",
        isRequired = false
    )

    object Mode : Arg<Mode.Platform>(
        id = ArgId.MODE,
        key = CompilerConfigurationKey(ArgId.MODE.name),
        valueDescription = "<${Platform.entries.joinToString(separator = "|", transform = { it.name })}>",
        description = "Mode decides the C++ file structure",
        isRequired = true
    ) {
        enum class Platform {
            ARDUINO,
            STD_CPP
        }
    }
}