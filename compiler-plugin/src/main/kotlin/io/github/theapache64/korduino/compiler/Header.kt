package io.github.theapache64.korduino.compiler

enum class Header(
    val fileName: String
) {
    Arduino("Arduino.h"),
    IoStream("iostream");
}

internal fun StringBuilder.containsHeader(header: Header): Boolean {
    return contains(header.fileName.includeStatement())
}

internal fun StringBuilder.addHeader(header: Header): StringBuilder {
    return this.insert(0, header.fileName.includeStatement() + "\n")
}