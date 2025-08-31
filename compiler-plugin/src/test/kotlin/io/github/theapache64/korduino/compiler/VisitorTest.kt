package io.github.theapache64.korduino.compiler

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test

class VisitorTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun test() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """
            import io.github.theapache64.korduino.core.Serial
            import io.github.theapache64.korduino.core.delay

            fun setup() {
                Serial.begin(115200)
            }

            fun loop() {
                Serial.println("Hello SP!")
                delay(1000)
            }
        """.trimIndent(),
        )

        val actualOutput = compile(listOf(input)).readActualOutput()

        val expectedOutput = """
            #include <Arduino.h>

            void setup() {
                io.github.theapache64.korduino.core.Serial.begin(115200);
            }
            void loop() {
                io.github.theapache64.korduino.core.Serial.println("Hello SP!");
                io.github.theapache64.korduino.core.delay(1000);
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}

@OptIn(ExperimentalCompilerApi::class)
private fun JvmCompilationResult.readActualOutput(): String {
    val pattern = "C\\+\\+ code generated at: '(.+)'".toRegex()
    val filePath = pattern.find(this.messages)?.groups[1]?.value ?: error("Couldn't find output file from messages")
    return File(filePath).readText()
}
