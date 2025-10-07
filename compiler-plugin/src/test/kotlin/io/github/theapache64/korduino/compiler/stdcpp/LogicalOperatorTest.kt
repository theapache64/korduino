package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class LogicalOperatorTest {

    @ParameterizedTest
    @ValueSource(strings = ["&&", "||"])
    fun basic(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 1 > 3 $operator 2 < 4
                if (x) {
                    println("Hello Kotlin!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                bool x = 1 > 3 $operator 2 < 4;
                if (x) {
                    std::cout << "Hello Kotlin!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["&&", "||"])
    fun inIf(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val a = 1
                val b = 2
                if (a > 0 $operator b > 0) {
                    println("Hello Kotlin!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int a = 1;
                int b = 2;
                if (a > 0 $operator b > 0) {
                    std::cout << "Hello Kotlin!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["&&", "||"])
    fun inElseIf(operator : String){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val a = 1
                val b = 2
                if (a < 0) {
                    println("Hello Kotlin!")
                } else if (a > 0 $operator b > 0) {
                    println("Hello Kotlin 2!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int a = 1;
                int b = 2;
                if (a < 0) {
                    std::cout << "Hello Kotlin!" << std::endl;
                } else if (a > 0 $operator b > 0) {
                    std::cout << "Hello Kotlin 2!" << std::endl;
                }
                return 0;
            }

        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["&&", "||"])
    fun inBothIfAndElseIf(operator :String){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val a = 1
                val b = 2
                if (a > 0 $operator b > 0) {
                    println("Hello Kotlin!")
                } else if (a > 0 $operator b < 0) {
                    println("Hello Kotlin 2!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int a = 1;
                int b = 2;
                if (a > 0 $operator b > 0) {
                    std::cout << "Hello Kotlin!" << std::endl;
                } else if (a > 0 $operator b < 0) {
                    std::cout << "Hello Kotlin 2!" << std::endl;
                }
                return 0;
            }

        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["&&", "||"])
    fun withFunction(operator : String){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun isPositive(num: Int): Boolean {
                return num > 0
            }

            fun main() : Int {
                val a = 1
                val b = 2
                if (isPositive(a) $operator isPositive(b)) {
                    println("Hello Kotlin!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            bool isPositive(int num) {
                return num > 0;
            }
            int main() {
                int a = 1;
                int b = 2;
                if (isPositive(a) $operator isPositive(b)) {
                    std::cout << "Hello Kotlin!" << std::endl;
                }
                return 0;
            }

        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun mixed(){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val a = 1
                val b = 2
                if (a > 0 && b > 0 || a < 0) {
                    println("Hello Kotlin!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int a = 1;
                int b = 2;
                if (a > 0 && b > 0 || a < 0) {
                    std::cout << "Hello Kotlin!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun complex(){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val a = 1
                val b = 2
                val c = 3
                if ((a > 0 && b > 0) || (c < 0 && a < 0)) {
                    println("Hello Kotlin!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int a = 1;
                int b = 2;
                int c = 3;
                if ((a > 0 && b > 0) || (c < 0 && a < 0)) {
                    std::cout << "Hello Kotlin!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }



}