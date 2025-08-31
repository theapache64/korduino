package io.github.theapache64.korduino.compiler

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicTest {
    @OptIn(ExperimentalCompilerApi::class)
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

@OptIn(ExperimentalCompilerApi::class)
private fun JvmCompilationResult.readActualOutput(): String {
    val pattern = "C\\+\\+ code generated at: '(.+)'".toRegex()
    val filePath = pattern.find(this.messages)?.groups[1]?.value ?: error("Couldn't find output file from messages")
    return File(filePath).readText()
}
