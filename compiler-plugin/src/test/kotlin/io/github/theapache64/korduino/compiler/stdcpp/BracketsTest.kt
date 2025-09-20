package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class BracketsTest {
    companion object {
        const val IMPORT_STATEMENTS = """
        """
    }

    @Test
    fun basic() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                val x = 100 / (20 + 2)
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int x = 100 / (20 + 2);
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}