package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.compileStdCpp
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class DynamicCppTest {
    companion object {
        const val IMPORT_STATEMENTS ="""
            import io.github.theapache64.korduino.core.cpp
        """
    }

    @Test
    fun basic() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                cpp(code = "int x; std::cin >> x;")
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                int x;
                std::cin >> x;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }



    @Test
    fun withHeaders() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                cpp(code = "hello();world();", "libHello", "libWorld") // headers as vararg
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            #include <libHello>
            #include <libWorld>
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                hello();
                world();
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

}