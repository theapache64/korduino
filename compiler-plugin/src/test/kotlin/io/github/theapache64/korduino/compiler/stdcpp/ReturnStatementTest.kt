package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.compileStdCpp
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class ReturnStatementTest {
    companion object {
        const val IMPORT_STATEMENTS = """
            
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
            fun getThatInt() : Int {
                return 123
            }

            fun main() : Int {
                println("Hello Kotlin!")
                return getThatInt()
            }

        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int getThatInt() {
                return 123;
            }
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                return getThatInt();
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun functionDefinitionAtWrongPosition() {

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
            int getThatInt() {
                return 123;
            }
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                return getThatInt();
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun functionDefinitionWithParamAtWrongPosition() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                return getThatInt(123)
            }
            
            fun getThatInt(x : Int) : Int {
                return x
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int getThatInt(int x) {
                return x;
            }
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                return getThatInt(123);
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun functionDefinitionWithParamAndValAtWrongPosition() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                val y =  getThatInt(123)
                return y
            }
            
            fun getThatInt(x : Int) : Int {
                return x
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int getThatInt(int x) {
                return x;
            }
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                int y = getThatInt(123);
                return y;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun overLoadedFunctionDefinitionAtWrongPosition() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                val x = getThatInt(456)
                return getThatInt()
            }
            
            fun getThatInt() : Int {
                return 123
            }
            
            fun getThatInt(x: Int) : Int {
                return x
            }
        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int getThatInt() {
                return 123;
            }
            int getThatInt(int x) {
                return x;
            }
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                int x = getThatInt(456);
                return getThatInt();
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun functionIntTakesIntParamAsReturn() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """$IMPORT_STATEMENTS
            fun main() : Int {
                println("Hello Kotlin!")
                return getThatInt(123)
            }
            
            fun getThatInt(x: Int) : Int {
                return x
            }

        """.trimIndent(),
        )

        val actualOutput = compileStdCpp(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int getThatInt(int x) {
                return x;
            }
            int main() {
                std::cout << "Hello Kotlin!" << std::endl;
                return getThatInt(123);
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }


}