package io.github.theapache64.korduino.compiler

enum class DataType(
    val type: String,
    val extraHeader: String? = null
) {
    Int("int"),
    Float("float"),
    Long("long long"),
    Boolean("bool"),
    Double("double"),
    Void("void"),
    String("std::string", "iostream");
}

private val commonDataTypes = mapOf(
    "kotlin.Int" to DataType.Int,
    "kotlin.Unit" to DataType.Void,
    "kotlin.String" to DataType.String,
    "kotlin.Float" to DataType.Float,
    "kotlin.Long" to DataType.Long,
    "kotlin.Double" to DataType.Double,
    "kotlin.Boolean" to DataType.Boolean,
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