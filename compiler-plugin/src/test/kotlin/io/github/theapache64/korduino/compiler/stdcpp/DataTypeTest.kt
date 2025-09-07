package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.compileStdCpp
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class DataTypeTest {
    companion object {
        const val IMPORT_STATEMENTS ="""
            
        """
    }

    @Test
    fun int() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                val a = 1
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            int main() {
                int a = 1;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun string() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                val a = "hello kotlin"
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                std::string a = "hello kotlin";
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun allPrimitives() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                val a = 1
                val b = 2f
                val c = "i am string"
                val d = 2000L
                val e = 3.14
                val f = false
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                int a = 1;
                float b = 2.0;
                std::string c = "i am string";
                long long d = 2000;
                double e = 3.14;
                bool f = false;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }



}