package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class WhenTest {

    @Test
    fun whenStatement() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 3
                when(x){
                    1 -> println("Hello Kotlin 1!")
                    2 -> println("Hello Kotlin 2!")
                    else -> println("Hello Kotlin else!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        // converts to if else blocks
        val expectedOutput = """
            #include <iostream>
            int main() {
                int x = 3;
                int tmp0_subject = x;
                if (tmp0_subject == 1) {
                    std::cout << "Hello Kotlin 1!" << std::endl;
                } else if (tmp0_subject == 2) {
                    std::cout << "Hello Kotlin 2!" << std::endl;
                } else {
                    std::cout << "Hello Kotlin else!" << std::endl;
                }
                return 0;
            }

        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun whenStatementWithMultipleConditions() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 3
                when(x){
                    1, 3 -> println("Hello Kotlin 1 or 3!")
                    2 -> println("Hello Kotlin 2!")
                    else -> println("Hello Kotlin else!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        // converts to if else blocks
        val expectedOutput = """
            #include <iostream>
            int main() {
                int x = 3;
                int tmp0_subject = x;
                if (tmp0_subject == 1 || tmp0_subject == 3) {
                    std::cout << "Hello Kotlin 1 or 3!" << std::endl;
                } else if (tmp0_subject == 2) {
                    std::cout << "Hello Kotlin 2!" << std::endl;
                } else {
                    std::cout << "Hello Kotlin else!" << std::endl;
                }
                return 0;
            }

        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun whenStatementWithoutSubject() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 3
                when{
                    x < 2 -> println("Hello Kotlin less than 2!")
                    x > 2 -> println("Hello Kotlin greater than 2!")
                    else -> println("Hello Kotlin else!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        // converts to if else blocks
        val expectedOutput = """
            #include <iostream>
            int main() {
                int x = 3;
                if (x < 2) {
                    std::cout << "Hello Kotlin less than 2!" << std::endl;
                } else if (x > 2) {
                    std::cout << "Hello Kotlin greater than 2!" << std::endl;
                } else {
                    std::cout << "Hello Kotlin else!" << std::endl;
                }
                return 0;
            }

        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}