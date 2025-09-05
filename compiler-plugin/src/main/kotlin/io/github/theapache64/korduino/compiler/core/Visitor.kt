package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.CodeBuilder
import io.github.theapache64.korduino.compiler.dataTypes
import io.github.theapache64.korduino.compiler.functions
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid


class Visitor(
    private val target: Arg.Platform.Target
) : IrVisitorVoid() {

    companion object {
        const val LINK_GITHUB_ISSUES =
            "Please raise a issue here if you think its a framework miss -> https://github.com/theapache64/korduino/issues/new"
    }

    private val codeBuilder = CodeBuilder()
    fun generateCode(): String = codeBuilder.getCode()

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitFunction(declaration: IrFunction) {

        if (declaration.parameters.isEmpty() && declaration.name.asString() != "<init>") {
            val dataTypeClassName = declaration.returnType.getClass()?.name?.asString()
            val returnType = dataTypes.get(key = dataTypeClassName)
                ?: error("Unsupported data type '$dataTypeClassName' (platform: $target). $LINK_GITHUB_ISSUES ")
            codeBuilder.appendLine("${returnType.type} ${declaration.name.asString()}() {")

            val header = returnType.extraHeader
            if (header != null && !codeBuilder.containsHeader(header)) {
                codeBuilder.addHeader(header)
            }

            super.visitFunction(declaration) // TODO: Explore: declaration.acceptChildrenVoid(this)
            codeBuilder.appendLine("}")
        }
    }

    override fun visitReturn(expression: IrReturn) {
        super.visitReturn(expression)
    }

    override fun visitReturn(expression: IrReturn, data: Nothing?) {
        codeBuilder.appendLine("return ${expression.toCodeString().joinToString(separator = ",")};")
        super.visitReturn(expression, data)
    }


    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(expression: IrCall) {
        val function = expression.symbol.owner
        val fqName = function.fqNameWhenAvailable?.asString()

        val argValues = mutableListOf<String>()
        for (expArg in expression.arguments) {
            if (expArg == null) continue
            argValues.addAll(expArg.toCodeString())
        }

        val (functionCall, headers) = if (fqName == "io.github.theapache64.korduino.core.cpp") {
            Pair(argValues[0], argValues.subList(1, argValues.size))
        } else {
            val cppFqName = functions[fqName]
                ?: error("Unsupported function name '$fqName' (platform: $target). $LINK_GITHUB_ISSUES ")

            Pair(cppFqName.fqName(argValues.joinToString(separator = ", ")), listOf(cppFqName.header.fileName))
        }

        val semicolon = if (!functionCall.trim().endsWith(";")) {
            ";"
        } else {
            ""
        }

        codeBuilder.appendLine("""    $functionCall$semicolon""") // TODO: Intent should by dynamic. Easiest way would be using a C++ code formatter library

        for (header in headers) {
            // Check if the header is present
            if (!codeBuilder.containsHeader(header)) {
                codeBuilder.addHeader(header)
            }
        }

        super.visitCall(expression)
    }

    private fun IrExpression.toCodeString(): List<String> {
        val argValues = mutableListOf<String>()
        when (this) {
            is IrConst -> {
                val value = value
                argValues.add(value as? String ?: value.toString())
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

            else -> error("Unhandled argValue type ${this::class.simpleName}")
        }
        return argValues
    }


}

