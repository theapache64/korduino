package io.github.theapache64.korduino.compiler.core

class CppParser() {
    companion object {
        private val FUNCTION_DEF_REGEX =
            "(?<returnType>\\w+)\\s(?<functionName>\\w+)\\((?<params>.*)\\)\\s\\{".toRegex()
        private val FUNCTION_CALL_REGEX = ".*?(?<functionName>\\w+)\\((?<params>.*)\\);".toRegex()
    }

    fun parse(cppCode: String): CppFile {
        val cppFunctions = mutableListOf<CppFunction>()
        val cppFunctionCalls = mutableMapOf<String, MutableList<CppFunctionCallSite>>()

        // start parsing
        val lines = cppCode.split("\n")
        for ((lineNo, line) in lines.withIndex()) {
            when {
                line.isFunctionDefinition() -> {
                    cppFunctions.add(line.toFunction(lineNo, lines))
                }

                line.isFunctionCall() -> {
                    // TOOD: generate function id and store in functioncalls mapo
                    val (_, functionName, params) = FUNCTION_CALL_REGEX.find(line)?.groupValues
                        ?: error("`$line` doesn't look like a function")
                    val paramCount = if (params.isBlank()) {
                        0
                    } else {
                        params.count { it == ',' } + 1
                    }
                    val functionId = CppFunction.generateFunctionId(functionName, paramCount)

                    // Going back and finding parent
                    var tempLineNo = lineNo
                    var parent: CppFunction? = null
                    while (parent == null) {
                        val prevLine = lines[--tempLineNo]
                        if (prevLine.isFunctionDefinition()) {
                            parent = prevLine.toFunction(tempLineNo, lines)
                        }
                    }

                    cppFunctionCalls.getOrPut(functionId, { mutableListOf() }).add(CppFunctionCallSite(lineNo, parent))
                }
            }
        }

        return CppFile(
            cppFunctions,
            cppFunctionCalls
        )
    }

    private fun String.isFunctionDefinition(): Boolean {
        return this.matches(FUNCTION_DEF_REGEX)
    }

    private fun String.toFunction(startLineNo: Int, lines: List<String>): CppFunction {
        val (_, returnType, functionName, params) = FUNCTION_DEF_REGEX.find(this)?.groupValues
            ?: error("`$this` doesn't look like a function")

        // find definition ending line number
        var endLineNo = -1
        for (tempLineNo in startLineNo..lines.lastIndex) {
            if (lines[tempLineNo] == "}") {
                endLineNo = tempLineNo
                break
            }
        }
        require(endLineNo != 1) { "Couldn't find end for function '$this'" }

        return CppFunction(
            returnType = returnType,
            name = functionName,
            cppParams = params.toParams(),
            startLineNo = startLineNo,
            endLineNo = endLineNo + 1,
        )
    }

    private fun String.toParams(): List<CppParam> {
        return this.split(",")
            .filter { it.isNotBlank() }
            .map { param ->
                val (dataType, name) = param.split(" ")
                CppParam(dataType, name)
            }
    }


    private fun String.isFunctionCall(): Boolean {
        return FUNCTION_CALL_REGEX.matches(this)
    }

}


data class CppParam(
    val dataType: String,
    val name: String
)

data class CppFunctionCallSite(
    val lineNo: Int,
    val parent: CppFunction,
)

data class CppFunction(
    val returnType: String,
    val name: String,
    val cppParams: List<CppParam>,
    val startLineNo: Int,
    val endLineNo: Int,
) {
    companion object {
        fun generateFunctionId(name: String, paramCount: Int): String {
            return "${name}_${paramCount}"
        }
    }

    fun id() = generateFunctionId(name, cppParams.size)
}

data class CppFile(
    val cppFunctions: List<CppFunction>,
    val functionCalls: Map<String, MutableList<CppFunctionCallSite>> // functionIds, and lineNo they get called
)


