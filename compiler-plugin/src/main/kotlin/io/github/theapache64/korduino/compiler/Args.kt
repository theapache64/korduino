package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.config.CompilerConfigurationKey

enum class ArgId {
    ENABLED,
    MODE,
    BUILD_DIR
}

sealed class Arg<T>(
    val id: ArgId,
    val valueDescription: String,
    val description: String,
    val isRequired: Boolean
) {
    val key: CompilerConfigurationKey<T> = CompilerConfigurationKey(id.name)

    object Enabled : Arg<Boolean>(
        id = ArgId.ENABLED,
        valueDescription = "<true|false>",
        description = "Enable the plugin",
        isRequired = false
    )

    object Mode : Arg<Mode.Platform>(
        id = ArgId.MODE,
        valueDescription = "<${Platform.entries.joinToString(separator = "|", transform = { it.name })}>",
        description = "Mode decides the C++ file structure",
        isRequired = false
    ) {
        enum class Platform {
            ARDUINO,
            STD_CPP
        }
    }

    object BuildDir : Arg<String>(
        id = ArgId.BUILD_DIR,
        valueDescription = "<absolute build directory path>",
        description = "Build directory where the C++ code will be generated",
        isRequired = true
    )
}