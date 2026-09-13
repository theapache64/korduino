package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.sun.tools.javac.tree.TreeInfo.symbol
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.stdcpp.DataTypeTest.Companion.IMPORT_STATEMENTS
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import io.github.theapache64.korduino.compiler.util.verifyRunnability
import kotlin.test.Test

class NullSafetyOperatorTest {


    @Test
    fun nullableFunction() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int { return 0 }
            fun foo() : String? {
                return null
            }


        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <optional>
            #include <string>
            int main() {
                return 0;
            }
            std::optional<std::string> foo() {
                return std::nullopt;
            }
            
        """.trimIndent().verifyRunnability()

        actualOutput.should.equal(expectedOutput)
    }
}