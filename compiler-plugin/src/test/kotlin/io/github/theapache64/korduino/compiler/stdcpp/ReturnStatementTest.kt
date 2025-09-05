package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.compileStdCpp
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class ReturnStatementTest {
    companion object {
        const val IMPORT_STATEMENTS ="""
            
        """
    }

    @Test
    fun basicInt() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun functionInt() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                return 0
            }
            
            fun getThatInt() : Int {
                return 123
            }

        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                return 0;
            }
            int getThatInt() {
                return 123;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun functionIntAsReturn() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                return getThatInt()
            }
            
            fun getThatInt() : Int {
                return 123
            }

        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                return getThatInt();
            }
            int getThatInt() {
                return 123;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }



}