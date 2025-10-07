package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class IndexedAccessOperatorTest {

    // @Test
    fun array() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val arr = arrayOf(1, 2, 3, 4, 5)
                val thirdElement = arr[2]
                println(thirdElement)
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            #include <array>
            int main() {
                std::array<int, 5> arr = {1, 2, 3, 4, 5};
                int thirdElement = arr[2];
                std::cout << thirdElement << std::endl;
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }
}