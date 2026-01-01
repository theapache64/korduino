package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class StringTest {
    companion object {
        const val IMPORT_STATEMENTS = """
            import io.github.theapache64.korduino.core.cpp
        """
    }

    @Test
    fun coutStringConcat() {

        val input = SourceFile.kotlin(
            "Main.kt",
            $$"""$$IMPORT_STATEMENTS
            fun main() : Int {
                val variable = 0
                println("Hello $variable Kotlin!")
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int variable = 0;
                std::cout << "Hello " << variable << " Kotlin!" << std::endl;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun coutMultipleStringConcat() {

        val input = SourceFile.kotlin(
            "Main.kt",
            $$"""$$IMPORT_STATEMENTS
            fun main() : Int {
                val variable = 0
                val var2 = 1
                println("A $variable B! $var2!")
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int variable = 0;
                int var2 = 1;
                std::cout << "A " << variable << " B! " << var2 << "!" << std::endl;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun coutMultipleVarStringConcat() {

        val input = SourceFile.kotlin(
            "Main.kt",
            $$"""$$IMPORT_STATEMENTS
            fun main() : Int {
                val variable = 0
                val var2 = 1
                println("A $variable B! $var2")
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int variable = 0;
                int var2 = 1;
                std::cout << "A " << variable << " B! " << var2 << std::endl;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun functionStringConcat() {

        val input = SourceFile.kotlin(
            "Main.kt",
            $$"""$$IMPORT_STATEMENTS
            fun main() : Int {
                val variable = 0
                someFunc("Hello $variable Kotlin!")
                return 0
            }
            fun someFunc(value : String) {
                println(value)
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            void someFunc(std::string value) {
                std::cout << value << std::endl;
            }
            int main() {
                int variable = 0;
                someFunc("Hello " + variable + " Kotlin!");
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}