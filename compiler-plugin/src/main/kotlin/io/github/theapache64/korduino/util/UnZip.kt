/*
 * Copyright 2021 Boil (https://github.com/theapache64/boil)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.theapache64.korduino.util


import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.*

fun Path.unzip(
    outputDir: Path
): Path {
    // Delete existing first
    ZipFile(this.toFile()).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            if (!entry.isDirectory) {
                zip.getInputStream(entry).use { input ->
                    val outputFile = outputDir / entry.name

                    with(outputFile) {
                        if (!outputFile.parent.exists()) {
                            parent.createDirectories()
                        }
                    }
                    outputFile.deleteIfExists()
                    outputFile.createFile()
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    return outputDir
}