package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.common.Baud
import io.github.theapache64.korduino.util.unzip
import java.nio.file.Path
import kotlin.io.path.*

interface TargetDirGenerator {
    fun create(cppFiles: List<Path>, buildDir: String): Path
}

class StdCppDirGenerator : TargetDirGenerator {
    override fun create(
        cppFiles: List<Path>,
        buildDir: String
    ): Path {
        // not doing any extra generation
        return Path(buildDir)
    }

}

class ArduinoDirGenerator(
    private val board: Arg.Board.Type,
    private val monitorSpeed: Baud = board.defaultMonitorSpeed,
    private val uploadSpeed: Baud = board.defaultUploadSpeed,
) : TargetDirGenerator {
    override fun create(cppFiles: List<Path>, buildDir: String): Path {
        val zipPath = TargetDirGenerator::class.java.getResource("/pio.zip")?.path
            ?: error("Couldn't find pio zip file in resources")
        var srcZipPath = Path(zipPath)
        if (!srcZipPath.exists()) {
            // Read from JAR otherwise.
            val zipStream = TargetDirGenerator::class.java
                .getResourceAsStream("/pio.zip") ?: error("Couldn't find pio zip file in compiler JAR")
            srcZipPath = createTempFile("pio", suffix = ".zip").apply {
                toFile().writeBytes(zipStream.readBytes())
            }
        }
        val pioParentDir = srcZipPath.unzip(Path(buildDir))
        updatePlatformIoIni(pioParentDir)
        val srcDir = pioParentDir.resolve("pio/src")
        for (cppFile in cppFiles) {
            cppFile.copyTo(srcDir.resolve(cppFile.name), overwrite = true)
        }
        return pioParentDir
    }

    /**
     * Update platformio.ini file based on plugin params
     */
    private fun updatePlatformIoIni(pioDir: Path) {
        val configFile = pioDir.resolve("pio/platformio.ini").toFile()
        val newConfig = """${board.config}
monitor_speed = ${monitorSpeed.value}
upload_speed = ${uploadSpeed.value}""".trimIndent()
        configFile.writeText(newConfig)
    }
}