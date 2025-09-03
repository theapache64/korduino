package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.compileStdCpp
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class PrintLnTest {
    @Test
    fun basic() {

        val input = SourceFile.Companion.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                println("Hello Kotlin!")
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun multiple() {

        val input = SourceFile.Companion.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                println("Hello Kotlin!")
                println("Hello Kotlin 2!")
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                std::cout << "Hello Kotlin 2!" << std::endl;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}