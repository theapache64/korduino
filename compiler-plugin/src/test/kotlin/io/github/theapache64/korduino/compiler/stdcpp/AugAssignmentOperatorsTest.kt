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

//  Augmented assignment operators `+=`, `-=`, `*=`, `/=`, `%=`)
class AugAssignmentOperatorsTest {

    @ParameterizedTest
    @ValueSource(strings = ["+=", "-=", "*=", "/=", "%="])
    fun basicAugAssignment(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 5
                a $operator 3
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 5;
                a $operator 3;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["+=", "-=", "*=", "/=", "%="])
    fun augAssignmentWithVariable(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 5
                var b = 3
                a $operator b
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 5;
                int b = 3;
                a $operator b;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["+=", "-=", "*=", "/=", "%="])
    fun augAssignmentInSequence(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 10
                var b = 5
                a $operator 2
                b $operator a
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 10;
                int b = 5;
                a $operator 2;
                b $operator a;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["+=", "-=", "*=", "/=", "%="])
    fun augAssignmentWithExpression(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 10
                var b = 3
                var c = 2
                a $operator (b + c)
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 10;
                int b = 3;
                int c = 2;
                a $operator (b + c);
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["+=", "-=", "*=", "/="])
    fun augAssignmentLongType(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Long {
                var a = 10L
                a $operator 3L
                return a
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            long long main() {
                long long a = 10;
                a $operator 3;
                return a;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = ["+=", "-=", "*=", "/="])
    fun augAssignmentDoubleType(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Double {
                var a = 10.5
                a $operator 2.5
                return a
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            double main() {
                double a = 10.5;
                a $operator 2.5;
                return a;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }


    @Test
    fun mixedAugAssignmentOperators() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 20
                var b = 10
                var c = 5
                a += b
                b -= c
                c *= 2
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 20;
                int b = 10;
                int c = 5;
                a += b;
                b -= c;
                c *= 2;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun augAssignmentWithIncrement() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 10
                var b = 5
                a += ++b
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 10;
                int b = 5;
                a += ++b;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun augAssignmentWithIncrementDecrement() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 10
                var b = 5
                a += ++b
                a -= b--
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 10;
                int b = 5;
                a += ++b;
                a -= b--;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    // TODO: Add more test cases with loops, objects etc once those constructs are ready
}