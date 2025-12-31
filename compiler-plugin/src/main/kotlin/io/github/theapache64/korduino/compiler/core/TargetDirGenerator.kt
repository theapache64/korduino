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
    private val monitorSpeed: Baud,
    private val uploadSpeed: Baud,
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
        val pioDir = srcZipPath.unzip(Path(buildDir))
        updatePlatformIoIni(pioDir)
        val srcDir = pioDir.resolve("pio/src")
        for (cppFile in cppFiles) {
            cppFile.copyTo(srcDir.resolve(cppFile.name), overwrite = true)
        }
        return pioDir
    }

    /**
     * Update platformio.ini file based on plugin params
     */
    private fun updatePlatformIoIni(pioDir: Path) {
        val configFile = pioDir.resolve("platformio.ini").toFile()
        val newConfig = """
            ${board.config}
            monitor_speed = $monitorSpeed
            upload_speed = $uploadSpeed
        """.trimIndent()
        configFile.writeText(newConfig)
    }
}