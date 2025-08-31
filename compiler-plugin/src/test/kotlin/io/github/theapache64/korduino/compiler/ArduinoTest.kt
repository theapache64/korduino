package io.github.theapache64.korduino.compiler

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test

class ArduinoTest {
    @Test
    fun beginAndPrintln() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun setup() {
                io.github.theapache64.korduino.core.Serial.begin(115200)
            }

            fun loop() {
                io.github.theapache64.korduino.core.Serial.println("Hello ESP!")
                io.github.theapache64.korduino.core.delay(1000)
            }
        """.trimIndent(),
        )

        val actualOutput = compile(listOf(input)).readActualOutput()

        val expectedOutput = """
            #include <Arduino.h>

            void setup() {
                Serial.begin(115200);
            }
            void loop() {
                Serial.println("Hello ESP!");
                delay(1000);
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}
