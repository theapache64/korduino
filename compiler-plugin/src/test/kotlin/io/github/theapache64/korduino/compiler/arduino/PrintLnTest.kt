package io.github.theapache64.korduino.compiler.arduino

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileArduinoSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class PrintLnTest {
    companion object {
        private const val IMPORT_STATEMENT = """
            import io.github.theapache64.korduino.core.Serial
        """
    }

    @Test
    fun basic() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENT
            fun setup() {
                Serial.begin(115200)
            }

            fun loop() {
                Serial.println("Hello ESP!")
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileArduinoSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.ARDUINO)

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

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENT
            fun setup() {
                Serial.begin(115200)
            }

            fun loop() {
                Serial.println("Hello ESP!")
                Serial.println("Hello ESP 2!")
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileArduinoSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.ARDUINO)

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