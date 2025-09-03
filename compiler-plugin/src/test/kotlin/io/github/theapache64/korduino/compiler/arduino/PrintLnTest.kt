package io.github.theapache64.korduino.compiler.arduino

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.compileArduino
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class PrintLnTest {
    @Test
    fun basic() {

        val input = SourceFile.Companion.kotlin(
            "Main.kt",
            """
            fun setup() {
                io.github.theapache64.korduino.core.Serial.begin(115200)
            }

            fun loop() {
                io.github.theapache64.korduino.core.Serial.println("Hello ESP!")
            }
        """.trimIndent(),
        )

        val actualOutput = compileArduino(listOf(input)).readActualOutput(Arg.Platform.Target.ARDUINO)

        val expectedOutput = """
            #include <Arduino.h>
            void setup() {
                Serial.begin(115200);
            }
            void loop() {
                Serial.println("Hello ESP!");
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun multiple() {

        val input = SourceFile.Companion.kotlin(
            "Main.kt",
            """
            fun setup() {
                io.github.theapache64.korduino.core.Serial.begin(115200)
            }

            fun loop() {
                io.github.theapache64.korduino.core.Serial.println("Hello ESP!")
                io.github.theapache64.korduino.core.Serial.println("Hello ESP 2!")
            }
        """.trimIndent(),
        )

        val actualOutput = compileArduino(listOf(input)).readActualOutput(Arg.Platform.Target.ARDUINO)

        val expectedOutput = """
            #include <Arduino.h>
            void setup() {
                Serial.begin(115200);
            }
            void loop() {
                Serial.println("Hello ESP!");
                Serial.println("Hello ESP 2!");
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}