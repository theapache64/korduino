package io.github.theapache64.korduino.compiler.arduino

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileArduinoSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class AnalogTest {
    companion object {
        val IMPORT_STATEMENTS = """
            import io.github.theapache64.korduino.core.*
        """.trimIndent()
    }

    @Test
    fun read() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS

            fun setup() {
                analogRead(32)
            }
            
            fun loop(){
                
            }

            """.trimIndent(),
        )

        val actualOutput =
            generateAndCompileArduinoSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.ARDUINO)

        val expectedOutput = """
            #include <Arduino.h>
            void setup() {
                analogRead(32);
            }
            void loop() {}
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}