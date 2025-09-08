package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.CodeBuilder
import io.github.theapache64.korduino.compiler.dataTypes
import io.github.theapache64.korduino.compiler.functions
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
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
    }

    private val codeBuilder = CodeBuilder()
    fun generateCode(): String = codeBuilder.getCode()

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
        if (header != null && !codeBuilder.containsHeader(header)) {
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
        codeBuilder.appendLine("return ${expression.toCodeString().joinToString(separator = "")};")
    }

    override fun visitVariable(declaration: IrVariable) {
        codeBuilder.appendLine("${declaration.toCodeString().joinToString(separator = "")};")
    }


    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(expression: IrCall) {
        val function = expression.symbol.owner
        val fqName = function.fqNameWhenAvailable?.asString()

        if (fqName == "kotlin.Int.inc" || fqName == "kotlin.Int.dec") return // already managed by visible variable

        val argValues = mutableListOf<String>()
        for (expArg in expression.arguments) {
            if (expArg == null) continue
            argValues.addAll(expArg.toCodeString())
        }

        val (functionCall, headers) = if (fqName == "io.github.theapache64.korduino.core.cpp") {
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
            Pair(cppFqName.fqName(argValues.joinToString(separator = ", ")), listOf(cppFqName.header.fileName))
        }

        val semicolon = if (!functionCall.trim().endsWith(";")) {
            ";"
        } else {
            ""
        }

        codeBuilder.appendLine("""    $functionCall$semicolon""")

        for (header in headers) {
            // Check if the header is present
            if (!codeBuilder.containsHeader(header)) {
                codeBuilder.addHeader(header)
            }
        }

        super.visitCall(expression)
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
                null
            }

            is IrGetEnumValueImpl -> {
                argValues.add(symbol.owner.name.asString())
            }

            is IrVarargImpl -> {
                elements.forEach {
                    val varArg = (it as IrConstImpl).value.toString()
                    argValues.add(varArg)
                }
                null
            }

            is IrReturnImpl -> {
                argValues.addAll(this.value.toCodeString())
            }

            is IrCallImpl -> {
                val isOperator = this.symbol.owner.isOperator
                if (isOperator) {
                    val opSymbol = when (val opName = this.symbol.owner.name.asString()) {
                        "plus" -> "+"
                        "minus" -> "-"
                        "div" -> "/"
                        "times" -> "*"
                        "rem" -> "%"
                        else -> error("Unknown operator `$opName`")
                    }
                    argValues.add(
                        this.arguments.mapNotNull { it?.toCodeString()?.joinToString(opSymbol) }.joinToString(opSymbol),
                    )
                } else {
                    argValues.addAll(
                        listOf(
                            this.symbol.owner.name.asString(), // function name
                            "(",
                            this.arguments.mapNotNull { it?.toCodeString()?.joinToString(",") }.joinToString(","),
                            ")"
                        )
                    )
                }

            }


            is IrSetValueImpl -> {
                val symbol = when (val name = this.origin?.debugName) {
                    "POSTFIX_INCR", "POSTFIX_DECR" -> "" // already handled
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
                argValues.addAll(this.statements.reversed().map { it.toCodeString().joinToString("") })
            }

            is IrVariableImpl -> {
                val typeFqName = type.classFqName?.asString()
                val dataType = dataTypes[typeFqName] ?: error("couldn't find dataType for `$typeFqName`")
                val variableName = name.asString()
                if (variableName == "<unary>") {
                    val debugName = (initializer as? IrGetValueImpl)?.origin?.debugName
                    val variableName = (initializer as IrGetValueImpl).symbol.owner.name.asString()
                    when (debugName) {
                        "POSTFIX_INCR" -> {
                            argValues.add("$variableName++")
                        }

                        "POSTFIX_DECR" -> {
                            argValues.add("$variableName--")
                        }

                        else -> {
                            error("Undefined getValue op `$debugName`")
                        }
                    }
                } else {
                    val variableCall = initializer?.toCodeString()?.joinToString(separator = "")
                    if (dataType.extraHeader != null) {
                        codeBuilder.addHeader(dataType.extraHeader)
                    }
                    argValues.add("${dataType.type} $variableName = $variableCall")
                }
            }


            else -> error("Unhandled argValue type ${this::class.simpleName}")
        }
        return argValues
    }


}

