package io.github.theapache64.korduino.compiler.pio

import com.github.theapache64.expekt.should
import io.github.theapache64.korduino.compiler.core.Pio
import org.jetbrains.kotlin.konan.file.createTempDir
import kotlin.io.path.*
import kotlin.test.Test

class PioTest {
    @Test
    fun sourceCreation() {
        val inputCppFile = createTempFile("sample_", ".cpp").apply {
            writeText(
                """
                #include <Arduino.h>

                void setup() {
                    Serial.begin(115200);
                    Serial.println("I 'was' Kotlin!");
                }

                void loop() {
                    Serial.println("Hello Kotlin!");
                    delay(2000);
                }
            """.trimIndent()
            )
        }
        val dir = Pio.create(listOf(inputCppFile), createTempDir("build").path)
        val actualCppFile = dir.resolve("pio/src/${inputCppFile.name}")

        actualCppFile.exists().should.`true`
        actualCppFile.readText().should.equal(inputCppFile.readText())
        inputCppFile.deleteIfExists()
    }
}