package io.github.theapache64.korduino.compiler

enum class Header(
    val fileName: String
) {
    Arduino("Arduino.h"),
    IoStream("iostream");

    fun includeStatement() = "#include <${fileName}>"
}

internal fun StringBuilder.contains(header: Header): Boolean {
    return contains(header.includeStatement())
}

internal fun StringBuilder.add(header: Header): StringBuilder {
    return this.insert(0, header.includeStatement() + "\n")
}