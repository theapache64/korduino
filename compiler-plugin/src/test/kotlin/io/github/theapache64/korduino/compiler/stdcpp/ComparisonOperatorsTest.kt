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

// Comparison operators (`>, >=, <, <=, ==, !=, ===, !==`)
class ComparisonOperatorsTest {

    @ParameterizedTest
    @ValueSource(strings = [">", ">=", "<", "<=", "==", "!="])
    fun basicComparison(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 5
                var result = a $operator 3
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 5;
                bool result = a $operator 3;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = [">", ">=", "<", "<=", "==", "!="])
    fun comparisonWithVariables(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 5
                var b = 3
                var result = a $operator b
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 5;
                int b = 3;
                bool result = a $operator b;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = [">", ">=", "<", "<=", "==", "!="])
    fun comparisonInIfStatement(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 5
                var b = 3
                if (a $operator b) {
                    return 1
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 5;
                int b = 3;
                if (a $operator b) {
                    return 1;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = [">", ">=", "<", "<=", "==", "!="])
    fun comparisonWithExpression(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 10
                var b = 3
                var c = 2
                var result = a $operator (b + c)
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
                bool result = a $operator (b + c);
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = [">", ">=", "<", "<=", "==", "!="])
    fun comparisonLongType(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Long {
                var a = 10L
                var result = a $operator 5L
                return a
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            long long main() {
                long long a = 10;
                bool result = a $operator 5;
                return a;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = [">", ">=", "<", "<=", "==", "!="])
    fun comparisonDoubleType(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Double {
                var a = 10.5
                var result = a $operator 5.2
                return a
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            double main() {
                double a = 10.5;
                bool result = a $operator 5.2;
                return a;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @ParameterizedTest
    @ValueSource(strings = [">", ">=", "<", "<=", "==", "!="])
    fun chainedComparisons(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 10
                var b = 5
                var c = 3
                var result1 = a $operator b
                var result2 = b $operator c
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 10;
                int b = 5;
                int c = 3;
                bool result1 = a $operator b;
                bool result2 = b $operator c;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun identityComparison() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 5
                var b = 5
                var result1 = a === b
                var result2 = a !== b
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 5;
                int b = 5;
                bool result1 = a == b;
                bool result2 = a != b;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun mixedComparisonOperators() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 20
                var b = 10
                var c = 5
                var result1 = a > b
                var result2 = b >= c
                var result3 = c < a
                var result4 = a == 20
                var result5 = b != c
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
                bool result1 = a > b;
                bool result2 = b >= c;
                bool result3 = c < a;
                bool result4 = a == 20;
                bool result5 = b != c;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun comparisonWithIncrementDecrement() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 10
                var b = 5
                var result1 = a > ++b
                var result2 = a-- < 12
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 10;
                int b = 5;
                bool result1 = a > ++b;
                bool result2 = a-- < 12;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun comparisonWithAugmentedAssignment() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var a = 10
                var b = 5
                a += 3
                var result = a > b
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 10;
                int b = 5;
                a += 3;
                bool result = a > b;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun booleanComparison() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                var flag1 = true
                var flag2 = false
                var result1 = flag1 == true
                var result2 = flag2 != flag1
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                bool flag1 = true;
                bool flag2 = false;
                bool result1 = flag1 == true;
                bool result2 = flag2 != flag1;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    // TODO: Add more test cases with loops, objects etc once those constructs are ready
}