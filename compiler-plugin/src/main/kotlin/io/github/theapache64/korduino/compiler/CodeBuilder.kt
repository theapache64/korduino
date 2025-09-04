package io.github.theapache64.korduino.compiler

class CodeBuilder() {
    private val stringBuilder = StringBuilder()
    var headerCursor: Int = 0;

    internal fun containsHeader(fileName: String): Boolean {
        return stringBuilder.contains(fileName.includeStatement())
    }

    internal fun addHeader(fileName: String): CodeBuilder {
        val newHeader = fileName.includeStatement() + "\n"
        stringBuilder.insert(headerCursor, newHeader)
        headerCursor += newHeader.length
        return this
    }

    fun getCode() = stringBuilder.toString()
    fun appendLine(string: String) : CodeBuilder {
        stringBuilder.appendLine(string)
        return this
    }
}