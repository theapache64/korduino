package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.util.unzip
import java.nio.file.Path
import kotlin.io.path.*

object Pio {
    @OptIn(ExperimentalPathApi::class)
    fun create(cppFiles: List<Path>, buildDir: String): Path {
        val zipPath = Pio::class.java.getResource("/pio.zip")?.path ?: error("Couldn't find pio zip file in resources")
        var srcZipPath = Path(zipPath)
        if (!srcZipPath.exists()) {
            // Read from JAR otherwise.
            val zipStream = Pio::class.java
                .getResourceAsStream("/pio.zip") ?: error("Couldn't find pio zip file in compiler JAR")
            srcZipPath = createTempFile("pio", suffix = ".zip").apply {
                toFile().writeBytes(zipStream.readBytes())
            }
        }
        val pioDir = srcZipPath.unzip(Path(buildDir))
        val srcDir = pioDir.resolve("pio/src")
        for (cppFile in cppFiles) {
            cppFile.copyTo(srcDir.resolve(cppFile.name), overwrite = true)
        }
        return pioDir
    }
}