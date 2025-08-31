package io.github.theapache64.korduino.compiler

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.Test

class StdCppTest {
    @Test
    fun printLn() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                println("Hello Kotlin!")
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput()

        val expectedOutput = """
            #include <iostream>
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}
