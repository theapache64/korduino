package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.stdcpp.DataTypeTest.Companion.IMPORT_STATEMENTS
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class IncrementDecrementOperatorTest {

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun postfix(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                a${symbol}
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                a${symbol};
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun postfixAsParam(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                return modify(a${symbol})
            }
            
            fun modify(a: Int) : Int {
                return a+1
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int modify(int a) {
                return a + 1;
            }
            int main() {
                int a = 1;
                return modify(a${symbol});
            }

        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun postfixAndReturn(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                val b = a${symbol}
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = a${symbol};
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun postfixDualAndReturn(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = a${symbol}
                b${symbol}
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = a${symbol};
                b${symbol};
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun prefix(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                ${symbol}a
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                ${symbol}a;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun prefixAndReturn(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                val b = ${symbol}a
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = ${symbol}a;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun prefixDualAndReturn(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = ${symbol}a
                ${symbol}b
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = ${symbol}a;
                ${symbol}b;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun mixedOperators() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = ++a
                var c = b--
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                int b = ++a;
                int c = b--;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun operatorInArithmeticExpression(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = 2 + a${symbol}
                return b
            }
        """.trimIndent(),
        )
        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)
        val expectedOutput = """
            int main() {
                int a = 1;
                int b = 2 + a${symbol};
                return b;
            }
            
        """.trimIndent()
        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun operatorLongType(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Long {
                var a = 1L
                a${symbol}
                return a
            }
        """.trimIndent(),
        )
        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)
        val expectedOutput = """
            long long main() {
                long long a = 1;
                a${symbol};
                return a;
            }
            
        """.trimIndent()
        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun operatorDoubleType(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Double {
                var a = 1.0
                ${symbol}a
                return a
            }
        """.trimIndent(),
        )
        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)
        val expectedOutput = """
            double main() {
                double a = 1.0;
                ${symbol}a;
                return a;
            }
            
        """.trimIndent()
        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["++", "--"])
    fun chainedOperator(symbol: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = a$symbol + ${symbol}a
                return b
            }
        """.trimIndent(),
        )
        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)
        val expectedOutput = """
            int main() {
                int a = 1;
                int b = a$symbol + ${symbol}a;
                return b;
            }
            
        """.trimIndent()
        actualOutput.should.equal(expectedOutput)
    }


    @Test
    fun chainedMixed() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = a++ + --a
                return b
            }
        """.trimIndent(),
        )
        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)
        val expectedOutput = """
            int main() {
                int a = 1;
                int b = a++ + --a;
                return b;
            }
            
        """.trimIndent()
        actualOutput.should.equal(expectedOutput)
    }


    @Test
    fun chainedMixedReverse() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 1
                var b = a-- + ++a
                return b
            }
        """.trimIndent(),
        )
        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)
        val expectedOutput = """
            int main() {
                int a = 1;
                int b = a-- + ++a;
                return b;
            }
            
        """.trimIndent()
        actualOutput.should.equal(expectedOutput)
    }


    // TODO: Add more tests cases with loops, object etc once those constructs are ready
}