package io.github.theapache64.korduino.compiler

enum class DataType(
    val type: String,
    val extraHeader: String? = null
) {
    INT("int"),
    VOID("void"),
    String("String","string");
}

private val commonDataTypes = mapOf(
    "kotlin.Int" to DataType.INT,
    "kotlin.Unit" to DataType.VOID,
    "kotlin.String" to DataType.String
)

private val arduinoDataTypes = mapOf<String, DataType>(

)

val dataTypes = commonDataTypes + arduinoDataTypes



internal fun StringBuilder.containsHeader(dataType: DataType): Boolean {
    return contains(dataType.type.includeStatement())
}

internal fun StringBuilder.addHeader(dataType: DataType): StringBuilder {
    return this.insert(0, dataType.type.includeStatement() + "\n")
}