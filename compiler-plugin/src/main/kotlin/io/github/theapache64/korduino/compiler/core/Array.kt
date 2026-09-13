package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.compiler.DataType
import io.github.theapache64.korduino.compiler.Either
import io.github.theapache64.korduino.compiler.Vector

data class ArrayInfo(
    val type: Either<DataType, Vector>,
    val size: Int,
    val variableName: String,
    val variableCall: String
)
