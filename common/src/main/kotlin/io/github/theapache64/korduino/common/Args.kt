package io.github.theapache64.korduino.common

import org.jetbrains.kotlin.config.CompilerConfigurationKey

enum class ArgId {
    ENABLED,
    PLATFORM,
    BUILD_DIR,
    BOARD,
    MONITOR_SPEED,
    UPLOAD_SPEED
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

    object Platform : Arg<Platform.Target>(
        id = ArgId.PLATFORM,
        valueDescription = "<${Target.entries.joinToString(separator = "|", transform = { it.name })}>",
        description = "Mode decides the C++ file structure",
        isRequired = false
    ) {
        enum class Target {
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

    object MonitorSpeed : Arg<Baud>(
        id = ArgId.MONITOR_SPEED,
        valueDescription = "<Log monitor speed>",
        description = "Value for `monitor_speed`. See https://docs.platformio.org/en/stable/projectconf/sections/env/options/monitor/monitor_speed.html",
        isRequired = false
    )

    object UploadSpeed : Arg<Baud>(
        id = ArgId.UPLOAD_SPEED,
        valueDescription = "<Upload speed>",
        description = "A connection speed (baud rate) which “uploader” tool uses when sending firmware to board. See https://docs.platformio.org/en/stable/projectconf/sections/env/options/upload/upload_speed.html",
        isRequired = false
    )

    object Board : Arg<Board.Type>(
        id = ArgId.BOARD,
        valueDescription = "<Board name>",
        description = "Your microcontroller board name (look behind the board)",
        isRequired = false,
    ) {
        enum class Type(
            val config: String,
        ) {
            ESP_32_DOIT_DEVKIT_V1(
                """
                [env:esp32doit-devkit-v1]
                platform = espressif32
                board = esp32doit-devkit-v1
                framework = arduino
            """.trimIndent()
            ),

            ESP_32_DEV(
                """
                [env:esp32dev]
                platform = espressif32
                board = esp32dev
                framework = arduino
            """.trimIndent()
            ),

        }
    }
}

