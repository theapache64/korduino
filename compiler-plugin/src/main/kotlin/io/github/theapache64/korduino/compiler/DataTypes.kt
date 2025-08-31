package io.github.theapache64.korduino.compiler

private val commonDataTypes = mapOf(
    "Int" to "int",
    "Unit" to "void",
)

private val arduinoDataTypes = mapOf<String, String>(

)

val dataTypes = commonDataTypes + arduinoDataTypes
