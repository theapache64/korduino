package io.github.theapache64.korduino.compiler

enum class Function(
    val fqName: (value: String?) -> String,
    val header: Header
) {
    // Arduino
    PrintLn({ "Serial.println(\"$it\")" }, Header.Arduino),
    Begin({ "Serial.begin($it)" }, Header.Arduino),
    Delay({ "delay($it)" }, Header.Arduino),

    // Std CPP
    COUT({ "std::cout << \"$it\" << std::endl" }, Header.IoStream)
}

val arduinoFunctions = mapOf<String, Function>(
    "kotlin.io.println" to Function.PrintLn,
    "io.github.theapache64.korduino.core.Serial.println" to Function.PrintLn,
    "io.github.theapache64.korduino.core.Serial.begin" to Function.Begin,
    "io.github.theapache64.korduino.core.delay" to Function.Delay
)

val stdCppFunctions = mapOf<String, Function>(
    "kotlin.io.println" to Function.COUT
)