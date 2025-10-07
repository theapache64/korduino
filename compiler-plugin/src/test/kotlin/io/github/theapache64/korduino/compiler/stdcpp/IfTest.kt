package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import kotlin.test.Test

class IfTest {
    @Test
    fun basicIf() {

        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 1 > 3
                if (x) {
                    println("Hello Kotlin!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                bool x = 1 > 3;
                if (x) {
                    std::cout << "Hello Kotlin!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun basicIfElse() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 1 > 3
                if (x) {
                    println("Hello Kotlin!")
                } else {
                    println("Hello Kotlin 2!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                bool x = 1 > 3;
                if (x) {
                    std::cout << "Hello Kotlin!" << std::endl;
                } else {
                    std::cout << "Hello Kotlin 2!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun baseIfElseElseIf() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 1 > 3
                if (x) {
                    println("Hello Kotlin!")
                } else if (!x) {
                    println("Hello Kotlin 2!")
                } else {
                    println("Hello Kotlin 3!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                bool x = 1 > 3;
                if (x) {
                    std::cout << "Hello Kotlin!" << std::endl;
                } else if (!x) {
                    std::cout << "Hello Kotlin 2!" << std::endl;
                } else {
                    std::cout << "Hello Kotlin 3!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun nestedIf(){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 1 > 3
                val y = 2 > 3
                if (x) {
                    println("Hello Kotlin!")
                    if(y){
                        println("Hello Kotlin 2!")
                    }
                } 
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                bool x = 1 > 3;
                bool y = 2 > 3;
                if (x) {
                    std::cout << "Hello Kotlin!" << std::endl;
                    if (y) {
                        std::cout << "Hello Kotlin 2!" << std::endl;
                    }
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun nestedIfElse(){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 1 > 3
                val y = 2 > 3
                if (x) {
                    println("Hello Kotlin!")
                    if(y){
                        println("Hello Kotlin 2!")
                    } else {
                        println("Hello Kotlin 3!")
                    }
                } else {
                    println("Hello Kotlin 4!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                bool x = 1 > 3;
                bool y = 2 > 3;
                if (x) {
                    std::cout << "Hello Kotlin!" << std::endl;
                    if (y) {
                        std::cout << "Hello Kotlin 2!" << std::endl;
                    } else {
                        std::cout << "Hello Kotlin 3!" << std::endl;
                    }
                } else {
                    std::cout << "Hello Kotlin 4!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun nestedIfElseIfElse(){
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun main() : Int {
                val x = 1 > 3
                val y = 2 > 3
                if (x) {
                    println("Hello Kotlin!")
                    if(y){
                        println("Hello Kotlin 2!")
                    } else if(!y){
                        println("Hello Kotlin 3!")
                    } else {
                        println("Hello Kotlin 4!")
                    }
                } else {
                    println("Hello Kotlin 5!")
                }
                return 0
            }
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            int main() {
                bool x = 1 > 3;
                bool y = 2 > 3;
                if (x) {
                    std::cout << "Hello Kotlin!" << std::endl;
                    if (y) {
                        std::cout << "Hello Kotlin 2!" << std::endl;
                    } else if (!y) {
                        std::cout << "Hello Kotlin 3!" << std::endl;
                    } else {
                        std::cout << "Hello Kotlin 4!" << std::endl;
                    }
                } else {
                    std::cout << "Hello Kotlin 5!" << std::endl;
                }
                return 0;
            }
            
        """.trimIndent()

        actualOutput.should.equal(expectedOutput)
    }

}