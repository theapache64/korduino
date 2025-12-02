package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import io.github.theapache64.korduino.compiler.util.verifyRunnability
import kotlin.test.Test

class IndexedAccessOperatorTest {

    @Test
    fun oneDimensionalIntArray() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val arr = arrayOf(1, 2, 3, 4, 5)
                val thirdElement = arr[2]
                println(thirdElement)
                return 0
            }
        """.trimIndent(),
        )

        val expectedOutput = """
            #include <array>
            #include <iostream>
            int main() {
                std::array<int, 5> arr = {1, 2, 3, 4, 5};
                int thirdElement = arr[2];
                std::cout << thirdElement << std::endl;
                return 0;
            }
            
        """.trimIndent().also{
            it.verifyRunnability()
        }
        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun oneDimensionalStringArray() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val arr = arrayOf("apple", "banana", "cherry", "date", "elderberry")
                val thirdElement = arr[2]
                println(thirdElement)
                return 0
            }
        """.trimIndent(),
        )

        val expectedOutput = """
            #include <array>
            #include <iostream>
            int main() {
                std::array<std::string, 5> arr = {"apple", "banana", "cherry", "date",
                                                  "elderberry"};
                std::string thirdElement = arr[2];
                std::cout << thirdElement << std::endl;
                return 0;
            }
            
        """.trimIndent().verifyRunnability()
        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun oneDimensionalAnyArray() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val arr = arrayOf(1, 2, 3, 4, 5, "apple", "banana")
                val thirdElement = arr[2]
                println(thirdElement)
                return 0
            }
        """.trimIndent(),
        )
        try {
            generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)
            assert(false) {
                "Expected IllegalStateException for mixed type array"
            } // Should not reach here
        } catch (_: Exception) {
            assert(true)
        }
    }

    @Test
    fun twoDimensionalIntArray(){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val arr = arrayOf(
                    arrayOf(1, 2, 3),
                    arrayOf(4, 5, 6, 7),
                    arrayOf(7, 8, 9)
                )
                val element = arr[1][2]
                println(element)
                return 0
            }
        """.trimIndent(),
        )

        val expectedOutput = """
            #include <array>
            #include <iostream>

            int main() {
                std::array<std::array<int, 4>, 3> arr = {{{1, 2, 3}, {4, 5, 6, 7}, {7, 8, 9}}};
                int element = arr[1][2];
                std::cout << element << std::endl;
                return 0;
            }
            
        """.trimIndent().verifyRunnability()

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)
        actualOutput.should.equal(expectedOutput)
    }
}