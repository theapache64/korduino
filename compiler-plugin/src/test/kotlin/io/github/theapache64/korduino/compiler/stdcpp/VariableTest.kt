package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileArduinoSourceCode
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class VariableTest {
    companion object {
        private const val IMPORT_STATEMENTS = """
        """
    }

    @Test
    fun read() {

        val input = SourceFile.kotlin(
            "Main.kt",
            $$"""$$IMPORT_STATEMENTS
            var globalVariable = 0
            fun loop() {
                println("Global variable is $globalVariable")
            }
        """.trimIndent(),
        )

        val actualOutput =
            generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int globalVariable = 0;
            void loop() {
                std::cout << "Global variable is " << globalVariable << std::endl;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun write() {

        val input = SourceFile.kotlin(
            "Main.kt",
            $$"""$$IMPORT_STATEMENTS
            var globalVariable = 0
            fun loop() {
                globalVariable = 3
                println("Global variable is $globalVariable")
            }
        """.trimIndent(),
        )

        val actualOutput =
            generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int globalVariable = 0;
            void loop() {
                globalVariable = 3;
                std::cout << "Global variable is " << globalVariable << std::endl;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}