package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.compiler.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid


class Visitor(
    private val platform: Arg.Mode.Platform
) : IrVisitorVoid() {

    companion object {
        const val LINK_GITHUB_ISSUES =
            "Please raise a issue here if you think its a framework miss -> https://github.com/theapache64/korduino/issues/new"
    }

    private val functions = when (platform) {
        Arg.Mode.Platform.ARDUINO -> arduinoFunctions
        Arg.Mode.Platform.STD_CPP -> stdCppFunctions
    }

    private val codeBuilder = StringBuilder()
    fun generateCode(): String = codeBuilder.toString()

    override fun visitFunction(declaration: IrFunction) {

        if (declaration.parameters.isEmpty() && declaration.name.asString() != "<init>") {
            val dataTypeClassName = declaration.returnType.getClass()?.name?.asString()
            val returnType = dataTypes.get(key = dataTypeClassName)
                ?: error("Unsupported data type '$dataTypeClassName' (platform: $platform). $LINK_GITHUB_ISSUES ")
            codeBuilder.appendLine("${returnType.type} ${declaration.name.asString()}() {")

            if (returnType.header != null && !codeBuilder.containsHeader(returnType)) {
                codeBuilder.addHeader(returnType)
            }

            super.visitFunction(declaration)
            codeBuilder.appendLine("}")
        }
    }


    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(expression: IrCall) {
        val function = expression.symbol.owner
        val fqName = function.fqNameWhenAvailable?.asString()
        val cppFqName = functions[fqName]
            ?: error("Unsupported function name '$fqName' (platform: $platform). $LINK_GITHUB_ISSUES ")
        val argument = expression.arguments[0]
        val value = when (argument) {
            is IrConst -> {
                val value = argument.value
                value as? String ?: value.toString()
            }

            is IrGetObjectValue -> {
                val value = (expression.arguments[function.parameters[1].symbol.owner] as IrConst).value
                value as? String ?: value.toString()
            }

            else -> ""
        }
        codeBuilder.appendLine("""    ${cppFqName.fqName(value)};""")

        // Check if the header is present
        if (!codeBuilder.containsHeader(cppFqName.header)) {
            codeBuilder.addHeader(cppFqName.header)
        }

        super.visitCall(expression)
    }


    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }
}

