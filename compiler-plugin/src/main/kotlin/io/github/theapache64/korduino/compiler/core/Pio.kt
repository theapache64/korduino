package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.util.unzip
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.name

object Pio {
    @OptIn(ExperimentalPathApi::class)
    fun create(cppFiles: List<Path>): Path {
        val zipPath = Pio::class.java.getResource("/pio.zip")?.path ?: error("Couldn't find pio zip file in resources")
        val srcZipPath = Path(zipPath)
        val pioDir = srcZipPath.unzip()
        val srcDir = pioDir.resolve("src")
        for (cppFile in cppFiles) {
            cppFile.copyTo(srcDir.resolve(cppFile.name))
        }
        return pioDir
    }
}