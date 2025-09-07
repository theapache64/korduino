package io.github.theapache64.korduino.compiler.arduino

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.compileArduino
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class PinModeTest {
    companion object{
        val IMPORT_STATEMENTS = """
            import io.github.theapache64.korduino.core.*
        """.trimIndent()
    }

    @Test
    fun basic() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS

            fun setup() {
                pinMode(Pin.D1, PinMode.INPUT)
                pinMode(32, PinMode.OUTPUT)
            }
            
            fun loop(){
            
            }

            """.trimIndent(),
        )

        val actualOutput = compileArduino(listOf(input)).readActualOutput(Arg.Platform.Target.ARDUINO)

        val expectedOutput = """
            #include <Arduino.h>
            void setup() {
                pinMode(D1, INPUT);
                pinMode(32, OUTPUT);
            }
            void loop() {}
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}