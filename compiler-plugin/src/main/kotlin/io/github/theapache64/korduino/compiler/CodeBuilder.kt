package io.github.theapache64.korduino.compiler

import io.github.theapache64.korduino.compiler.core.CppFormatter
import io.github.theapache64.korduino.compiler.core.CppParser

class CodeBuilder(
    private val cppFormatter: CppFormatter = CppFormatter(), private val cppParser: CppParser = CppParser()
) {
    private val stringBuilder = StringBuilder()
    var headerCursor: Int = 0;

    private fun containsHeader(fileName: String): Boolean {
        return stringBuilder.contains(fileName.includeStatement())
    }

    internal fun addHeader(fileName: String): CodeBuilder {
        if (containsHeader(fileName)) return this
        val newHeader = fileName.includeStatement() + "\n"
        stringBuilder.insert(headerCursor, newHeader)
        headerCursor += newHeader.length
        return this
    }

    fun getCode(): String {
        return stringBuilder.formatCppCode().applyCppRules().formatCppCode().toString()
    }

    fun appendLine(string: String): CodeBuilder {
        val finalString = string.trim()
        if (finalString.isEmpty()) return this
        stringBuilder.appendLine(string.addSemiColonIfNeeded())
        return this
    }

    private fun StringBuilder.formatCppCode(): StringBuilder {
        val unformattedCode = this.toString()
        this.clear()
        this.append(cppFormatter.format(unformattedCode))
        return this
    }

    private fun StringBuilder.applyCppRules(): StringBuilder {
        //rule #1: a function should be defined above its call site; so let's rearrange it accordingly if such instances exist
        val result = cppParser.parse(this.toString())
        for (cppFunction in result.cppFunctions) {
            val minCallSite = result.functionCalls[cppFunction.id()]?.minBy { it.lineNo }
            if (minCallSite != null) {
                if (minCallSite.lineNo < cppFunction.startLineNo) {
                    // wrong position - rearrange
                    val newCode = cppFormatter.moveFunction(
                        function = cppFunction, fullCode = this, moveToLineNo = minCallSite.parent.startLineNo
                    )
                    this.clear()
                    this.append(newCode)
                    return this.applyCppRules() // Do it again
                }
            }
        }
        return this
    }

    fun addHeaders(headers: List<String>) {
        for (header in headers) {
            addHeader(header)
        }
    }
}

fun String.addSemiColonIfNeeded(): String {
    val string = this.trim()
    if (string.isEmpty() ||
        string.endsWith(";") ||
        string.endsWith("{") ||
        string.endsWith("}")
    ) return this
    return "$this;"
}


