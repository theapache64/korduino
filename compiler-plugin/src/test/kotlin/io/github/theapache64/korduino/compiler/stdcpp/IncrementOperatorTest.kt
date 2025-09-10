package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.stdcpp.DataTypeTest.Companion.IMPORT_STATEMENTS
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class IncrementOperatorTest {

    @Test
    fun increment() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                a++
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                a++;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun incrementAndReturn() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                val b = a++
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = a++;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun incrementAndReturnAndIncrement() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = a++
                b++
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = a++;
                b++;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun prefixIncrement() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                ++a
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                ++a;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun prefixIncrementAndReturn() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                val b = ++a
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = ++a;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun prefixIncrementAndReturnAndIncrement() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = ++a
                ++b
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = ++a;
                ++b;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun mixedPrefixAndPostfixIncrement() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = ++a
                var c = b++
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = ++a;
                int c = b++;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

}