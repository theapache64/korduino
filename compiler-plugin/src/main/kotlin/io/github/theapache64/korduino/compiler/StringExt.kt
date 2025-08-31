package io.github.theapache64.korduino.compiler

fun String.includeStatement() = "#include <${this}>"