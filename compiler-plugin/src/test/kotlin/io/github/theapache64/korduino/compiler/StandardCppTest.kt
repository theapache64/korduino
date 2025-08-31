package io.github.theapache64.korduino.compiler

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test

class StandardCppTest {
    @Test
    fun beginAndPrintln() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun setup() {
                io.github.theapache64.korduino.core.Serial.begin(115200)
            }

            fun loop() {
                println("Hello ESP!")
            }
        """.trimIndent(),
        )

        val actualOutput = compileArduino(listOf(input)).readActualOutput()

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
