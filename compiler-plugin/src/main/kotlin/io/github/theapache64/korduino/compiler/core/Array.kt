package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.compiler.DataType

data class ArrayInfo(
    val dataType: DataType,
    val size: Int,
    val variableName: String,
    val variableCall : String
)
