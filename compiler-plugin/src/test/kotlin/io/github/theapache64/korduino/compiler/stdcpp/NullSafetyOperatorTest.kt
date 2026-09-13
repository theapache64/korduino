package io.github.theapache64.korduino.compiler.stdcpp

import com.github.theapache64.expekt.should
import com.tschuchort.compiletesting.SourceFile
import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.util.generateAndCompileCppSourceCode
import io.github.theapache64.korduino.compiler.util.readActualOutput
import io.github.theapache64.korduino.compiler.util.verifyRunnability
import kotlin.test.Test

class NullSafetyOperatorTest {


    @Test
    fun nullableFunctionDef() {
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

    @Test
    fun nullableFunctionDef2() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun foo(shouldReturnNull: Boolean) : String? {
                var value : String? = ""
                if(shouldReturnNull){
                    value = null
                }else{
                    value = "bar"
                }
                return value
            }
            fun main() : Int {
                return 0 
            }
            
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <optional>
            #include <string>
            std::optional<std::string> foo(bool shouldReturnNull) {
                std::optional<std::string> value = "";
                if (shouldReturnNull) {
                    value = std::nullopt;
                } else {
                    value = "bar";
                }
                return value;
            }
            int main() {
                return 0;
            }
            
        """.trimIndent().verifyRunnability()

        actualOutput.should.equal(expectedOutput)
    }

    @Test
    fun nullableFunctionDefPlusCall() {
        val input = SourceFile.kotlin(
            "Main.kt",
            """
            fun foo(shouldReturnNull: Boolean) : String? {
                var value : String? = ""
                if(shouldReturnNull){
                    value = null
                }else{
                    value = "bar"
                }
                return value
            }
            fun main() : Int { 
                val nullValue = foo(shouldReturnNull = true)
                val nonNullValue = foo(shouldReturnNull = false)
                val x = nullValue?.toString()
                println(x)                
                val y = nonNullValue?.toString()
                println(y)
                return 0 
            }
            
        """.trimIndent(),
        )

        val actualOutput = generateAndCompileCppSourceCode(listOf(input)).readActualOutput(Arg.Platform.Target.STD_CPP)

        val expectedOutput = """
            #include <iostream>
            #include <optional>
            #include <string>
            std::optional<std::string> foo(bool shouldReturnNull) {
                std::optional<std::string> value = "";
                if (shouldReturnNull) {
                    value = std::nullopt;
                } else {
                    value = "bar";
                }
                return value;
            }
            int main() {
                std::optional<std::string> nullValue = foo(true);
                std::optional<std::string> nonNullValue = foo(false);
                std::optional<std::string> x = nullValue.has_value() ? std::optional<std::string>(nullValue.value()) : std::nullopt;
                std::cout << (x.has_value() ? x.value() : "null") << std::endl;
                std::optional<std::string> y = nonNullValue.has_value() ? std::optional<std::string>(nonNullValue.value()) : std::nullopt;
                std::cout << (y.has_value() ? y.value() : "null") << std::endl;
                return 0;
            }
        """.trimIndent().verifyRunnability()

        actualOutput.should.equal(expectedOutput)
    }
}