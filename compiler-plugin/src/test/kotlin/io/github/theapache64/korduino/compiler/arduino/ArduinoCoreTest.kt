package io.github.theapache64.korduino.compiler.arduino

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileArduinoSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class ArduinoCoreTest {
    companion object {
        private const val IMPORT_STATEMENTS = """
            import io.github.theapache64.korduino.core.delay
        """
    }

    @Test
    fun missingSetup() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun setup(){}
            fun loop() {
                delay(1000)
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileArduinoSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.ARDUINO)

        val expectedOutput = """
            #include <Arduino.h>
            void setup() {}
            void loop() {
                delay(1000);
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}