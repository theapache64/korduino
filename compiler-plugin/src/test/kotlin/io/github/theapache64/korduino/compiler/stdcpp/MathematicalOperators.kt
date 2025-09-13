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

class MathematicalOperators {

    @ParameterizedTest
    @ValueSource(strings = ["+", "-", "*", "/", "%"])
    fun basic(operator: String) {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                val a = 1 $operator 2
                val b = a $operator 5
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1 $operator 2;
                int b = a $operator 5;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun multiple() {
        val operator = "+"
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                val a = 1 $operator 2 $operator 3
                val b = a $operator 5 $operator 2
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1 $operator 2 $operator 3;
                int b = a $operator 5 $operator 2;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

}