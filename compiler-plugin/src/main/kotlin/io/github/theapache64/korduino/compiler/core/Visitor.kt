package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.CodeBuilder
import io.github.theapache64.korduino.compiler.dataTypes
import io.github.theapache64.korduino.compiler.functions
import org.jetbrains.kotlin.backend.jvm.ir.getIoFile
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.ANDAND
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.EXCLEQ
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.IF
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.OROR
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.POSTFIX_DECR
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.POSTFIX_INCR
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.PREFIX_DECR
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.PREFIX_INCR
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.WHEN
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid


class Visitor(
    private val target: Arg.Platform.Target,
) : IrVisitorVoid() {

    companion object {
        const val LINK_GITHUB_ISSUES =
            "Please raise a issue here if you think its a framework miss -> https://github.com/theapache64/korduino/issues/new"

        /**
         * A special function used to put raw cpp code in the generated file.
         */
        private const val FUNCTION_RAW_CPP = "io.github.theapache64.korduino.core.cpp"
    }

    var sourceText: String = ""

    private val codeBuilder = CodeBuilder()
    fun generateCode(): String = codeBuilder.getCode()

    override fun visitFile(declaration: IrFile) {
        sourceText = declaration.getIoFile()?.readText() ?: ""
        super.visitFile(declaration)
    }

    override fun visitElement(element: IrElement) {
        // This is the first function to get a hit. Here we're telling to recursively loop through each element
        element.acceptChildrenVoid(this)
    }

    override fun visitFunction(declaration: IrFunction) {
        val functionName = declaration.name.asString()
        val dataTypeClassName = declaration.returnType.classFqName?.asString()
        val returnType = dataTypes.get(key = dataTypeClassName)
            ?: error("Unsupported data type '$dataTypeClassName' (platform: $target). $LINK_GITHUB_ISSUES ")
        val params = extractParams(declaration)
        codeBuilder.appendLine("${returnType.type} $functionName($params) {")

        val header = returnType.extraHeader
        if (header != null) {
            codeBuilder.addHeader(header)
        }

        super.visitFunction(declaration) // TODO: Explore: declaration.acceptChildrenVoid(this)
        codeBuilder.appendLine("}")
    }


    private fun extractParams(declaration: IrFunction): String {
        return declaration.parameters.joinToString(",") {
            "${dataTypes[it.type.classFqName?.asString()]?.type} ${it.name}"
        }
    }

    override fun visitReturn(expression: IrReturn) {
        codeBuilder.appendLine(expression.toCodeString().joinToString(separator = " "))
    }

    override fun visitVariable(declaration: IrVariable) {
        codeBuilder.appendLine("${declaration.toCodeString().joinToString(separator = " ")};")
    }

    override fun visitWhen(expression: IrWhen) {
        codeBuilder.appendLine(expression.toCodeString().joinToString(separator = " "))
    }


    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(expression: IrCall) {
        val (functionCall, headers, semicolon) = expression.toFunctionCall()

        if (functionCall.isNotBlank()) {
            codeBuilder.appendLine("$functionCall$semicolon")
        } else {
            // Couldn't figure out the function call, so visiting children to avoid missing anything
            super.visitCall(expression)
        }

        codeBuilder.addHeaders(headers)
    }

    private fun IrCall.toFunctionCall(): Triple<String, List<String>, String> {
        val function = symbol.owner
        val fqName = function.fqNameWhenAvailable?.asString()
        val argValues = mutableListOf<String>()
        for (expArg in arguments) {
            if (expArg == null) continue
            argValues.addAll(expArg.toCodeString())
        }

        val (functionCall, headers) = if (fqName == FUNCTION_RAW_CPP) {
            val code = argValues[0].let { code ->
                code.substring(1, code.lastIndex - 1) // stripping out `"`
            }
            Pair(code, argValues.subList(1, argValues.size))
        } else if (fqName != null && !functions.containsKey(fqName)) {
            // unknown function
            Pair("$fqName(${argValues.joinToString(separator = ", ")})", emptyList())
        } else {
            // known function
            val cppFqName = functions[fqName]
                ?: error("Unsupported function name '$fqName' (platform: $target). $LINK_GITHUB_ISSUES ")
            val headers = if (cppFqName.header != null) listOf(cppFqName.header.fileName) else emptyList()
            val funCall = cppFqName.fqName(argValues.joinToString(separator = ", "))
            Pair(funCall, headers)
        }

        val semicolon = if (functionCall.isBlank() || functionCall.trim().endsWith(";")) {
            ""
        } else {
            ";"
        }

        return Triple(functionCall, headers, semicolon)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun IrStatement.toCodeString(): List<String> {
        val argValues = mutableListOf<String>()
        when (this) {
            is IrConst -> {
                val value = if (kind == IrConstKind.String) {
                    "\"${value}\""
                } else {
                    "$value"
                }
                argValues.add(value)
            }

            is IrGetObjectValue -> {
                null // TODO: Remove this
            }

            is IrGetEnumValueImpl -> {
                argValues.add(symbol.owner.name.asString())
            }

            is IrVarargImpl -> {
                elements.forEach {
                    val varArg = (it as IrConstImpl).value.toString()
                    argValues.add(varArg)
                }
            }

            is IrReturnImpl -> {
                argValues.add("return ${this.value.toCodeString().joinToString(" ")};")
            }

            is IrCallImpl -> {
                val isOperator = this.symbol.owner.isOperator || this.symbol.owner.origin.name == "OPERATOR"
                if (isOperator) {
                    val opSymbol = when (val opName = this.symbol.owner.name.asString()) {
                        // TODO: use constants from IR artifact here
                        "plus" -> "+"
                        "minus" -> "-"
                        "div" -> "/"
                        "times" -> "*"
                        "rem" -> "%"
                        "greater" -> ">"
                        "greaterOrEqual" -> ">="
                        "less" -> "<"
                        "lessOrEqual" -> "<="
                        "EQEQ" -> {
                            if (this.origin?.debugName == EXCLEQ.debugName) {
                                "!="
                            } else {
                                "=="
                            }
                        }

                        "not" -> {
                            if (this.origin?.debugName == EXCLEQ.debugName) {
                                ""
                            } else {
                                "!"
                            }
                        }

                        else -> error("Unknown operator `$opName`")
                    }

                    val (startBracket, endBracket) = getBrackets(startOffset, endOffset)

                    if (this.arguments.size == 1) {
                        argValues.add(
                            "$opSymbol$startBracket${
                                this.arguments.mapNotNull { it?.toCodeString()?.joinToString(opSymbol) }
                                    .joinToString(" $opSymbol ")
                            }$endBracket")
                    } else {
                        argValues.add(
                            "$startBracket${
                                this.arguments.mapNotNull { it?.toCodeString()?.joinToString(opSymbol) }
                                    .joinToString(" $opSymbol ")
                            }$endBracket")
                    }
                } else {
                    /*argValues.addAll(
                        listOf(
                            this.symbol.owner.name.asString(), // function name
                            "(",
                            this.arguments.mapNotNull { it?.toCodeString()?.joinToString(",") }.joinToString(","),
                            ")"
                        )
                    )*/

                    val (functionCall, headers, semicolon) = this.toFunctionCall()
                    argValues.addAll(listOf(functionCall, semicolon))

                    if (headers.isNotEmpty()) {
                        codeBuilder.addHeaders(headers)
                    }
                }

            }


            is IrSetValueImpl -> {
                val symbol = when (val name = this.origin?.debugName) {
                    POSTFIX_INCR.debugName, POSTFIX_DECR.debugName, PREFIX_INCR.debugName, PREFIX_DECR.debugName -> "" // already handled these two
                    else -> error("Unhandled setValue call `$name`")
                }
                argValues.add(symbol)
            }

            is IrGetValueImpl -> {
                val symbol = when (val name = this.symbol.owner.name.asString()) {
                    "<unary>" -> "" // already handled
                    else -> name
                }
                argValues.add(symbol)
            }

            is IrBlockImpl -> {
                val symbol = when (this.origin?.debugName) {
                    PREFIX_INCR.debugName -> "++"
                    PREFIX_DECR.debugName -> "--"
                    else -> null
                }
                if (symbol != null) {
                    val variableName =
                        ((this.statements.firstOrNull() as? IrDeclarationReference)?.symbol?.owner as? IrDeclarationWithName)?.name?.asString()
                    require(variableName != null) { "Couldn't find variable name" }
                    argValues.add("$symbol$variableName")
                } else {
                    argValues.addAll(this.statements.map { it.toCodeString().joinToString(" ") })
                }
            }

            is IrVariableImpl -> {
                val typeFqName = type.classFqName?.asString()
                val dataType = dataTypes[typeFqName] ?: error("couldn't find dataType for `$typeFqName`")
                val variableName = name.asString()
                if (variableName == "<unary>") {
                    val debugName = (initializer as? IrGetValueImpl)?.origin?.debugName
                    val variableName = (initializer as IrGetValueImpl).symbol.owner.name.asString()
                    val statement = when (debugName) {
                        POSTFIX_INCR.debugName -> "$variableName++"
                        POSTFIX_DECR.debugName -> "$variableName--"
                        PREFIX_INCR.debugName -> "++$variableName"
                        PREFIX_DECR.debugName -> "--$variableName"
                        else -> {
                            error("Undefined getValue op `$debugName`")
                        }
                    }
                    argValues.add(statement)
                } else {
                    val variableCall = initializer?.toCodeString()?.joinToString(separator = "")
                    if (dataType.extraHeader != null) {
                        codeBuilder.addHeader(dataType.extraHeader)
                    }
                    argValues.add("${dataType.type} $variableName = $variableCall")
                }
            }

            is IrWhenImpl -> {
                when (this.origin?.debugName) {
                    OROR.debugName, ANDAND.debugName -> {
                        val conditionSymbol = if (this.origin?.debugName == OROR.debugName) "||" else "&&"
                        val (startBracket, endBracket) = getBrackets(startOffset, endOffset)
                        val condition = this.branches
                            .map {
                                when (it) {
                                    is IrBranchImpl -> it.condition.toCodeString()
                                    is IrElseBranchImpl -> {
                                        if (it.result is IrConstImpl) {
                                            // Get the variable using offset values
                                            listOf(
                                                sourceText.substring(it.startOffset, it.endOffset)
                                            )
                                        } else {
                                            it.result.toCodeString()
                                        }
                                    }

                                    else -> error("Unknown branch type ${it::class.simpleName}")
                                }.joinToString(conditionSymbol)
                            }
                            .filter { it.isNotBlank() }
                            .joinToString(" $conditionSymbol ")
                        argValues.add("$startBracket$condition$endBracket")
                    }

                    IF.debugName, WHEN.debugName -> {
                        this.branches
                            .forEachIndexed { index, branch ->
                                when (branch) {
                                    is IrBranchImpl -> {
                                        val condition = branch.condition.toCodeString().joinToString(" ")
                                        val ifOrElseIf = if (index == 0) "if" else "else if"
                                        argValues.add("$ifOrElseIf($condition){")
                                        val result = branch.result.toCodeString()
                                        argValues.addAll(result)
                                        argValues.add("}")
                                    }

                                    is IrElseBranchImpl -> {
                                        argValues.add("else {")
                                        val result = branch.result.toCodeString()
                                        argValues.addAll(result)
                                        argValues.add("}")
                                    }

                                    else -> error("Unknown branch type ${branch::class.simpleName}")
                                }
                            }
                    }



                    else -> error("Unhandled IrWhenImpl origin `${this.origin?.debugName}`")
                }
            }


            else -> error("Unhandled argValue type ${this::class.simpleName}")
        }
        return argValues.filter { it.isNotBlank() }
    }

    private fun getBrackets(
        startOffset: Int,
        endOffset: Int
    ): Pair<String, String> {
        var startBracket = ""
        var endBracket = ""
        if (isPrevCharIsBracket(startOffset) && isNextCharIsBracket(endOffset) && !isIfStatement(startOffset)) {
            startBracket = "("
            endBracket = ")"
        }
        return Pair(startBracket, endBracket)
    }

    private fun isIfStatement(startOffset: Int): Boolean {
        var index = startOffset - 1
        while (index >= 0) {
            val char = sourceText[index]
            if (char.isWhitespace() || char == '(') {
                index--
                continue
            }
            return sourceText.substring(index - 1, index + 1).also {
                println("QuickTag: Visitor:isIfStatement: comparing with `$it`")
            } == "if"
        }
        return false
    }

    private fun isPrevCharIsBracket(startOffset: Int): Boolean {
        var index = startOffset - 1
        while (index >= 0) {
            val char = sourceText[index]
            if (char.isWhitespace()) {
                index--
                continue
            }
            return char == '('
        }
        return false
    }

    private fun isNextCharIsBracket(endOffset: Int): Boolean {
        var index = endOffset
        while (index < sourceText.length) {
            val char = sourceText[index]
            if (char.isWhitespace()) {
                index++
                continue
            }
            return char == ')'
        }
        return false
    }
}
