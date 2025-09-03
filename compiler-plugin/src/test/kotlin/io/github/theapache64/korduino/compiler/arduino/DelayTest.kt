package io.github.theapache64.korduino.compiler.arduino

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.compileArduino
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class DelayTest {
    @Test
    fun basic() {

        val input = SourceFile.Companion.kotlin(
            "Main.kt",
            """
            fun loop() {
                io.github.theapache64.korduino.core.delay(1000)
            }
        """.trimIndent(),
        )

        val actualOutput = compileArduino(listOf(input)).readActualOutput(Arg.Platform.Target.ARDUINO)

        val expectedOutput = """
            #include <Arduino.h>
            void loop() {
                delay(1000);
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}