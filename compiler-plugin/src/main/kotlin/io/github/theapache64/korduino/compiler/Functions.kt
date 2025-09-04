package io.github.theapache64.korduino.compiler

enum class Function(
    val fqName: (value: String?) -> String,
    val header: Header
) {
    // Arduino
    PrintLn({ "Serial.println(\"$it\")" }, Header.Arduino),
    Begin({ "Serial.begin($it)" }, Header.Arduino),
    Delay({ "delay($it)" }, Header.Arduino),
    PinMode({ "pinMode($it)" }, Header.Arduino),

    // Std CPP
    COUT({ "std::cout << \"$it\" << std::endl" }, Header.IoStream),
}

val functions = mapOf<String, Function>(
    "kotlin.io.println" to Function.COUT,
    "io.github.theapache64.korduino.core.Serial.println" to Function.PrintLn,
    "io.github.theapache64.korduino.core.Serial.begin" to Function.Begin,
    "io.github.theapache64.korduino.core.delay" to Function.Delay,
    "io.github.theapache64.korduino.core.pinMode" to Function.PinMode,
)