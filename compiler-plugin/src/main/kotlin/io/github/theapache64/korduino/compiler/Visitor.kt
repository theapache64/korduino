package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid

val functionMap = mapOf(
    "kotlin.io.println" to "Serial.println",
    "io.github.theapache64.korduino.core.Serial.begin" to "Serial.begin",
    "io.github.theapache64.korduino.core.Serial.println" to "Serial.println",
    "io.github.theapache64.korduino.core.delay" to "delay"
)

class Visitor : IrVisitorVoid() {

    private val codeBuilder = StringBuilder()

    fun generateCode(): String = codeBuilder.toString()


    override fun visitModuleFragment(declaration: IrModuleFragment) {
        codeBuilder.appendLine("#include <Arduino.h>")
        codeBuilder.appendLine("")
        super.visitModuleFragment(declaration)
    }


    override fun visitFunction(declaration: IrFunction) {

        if (declaration.parameters.isEmpty() && declaration.name.asString() != "<init>") {
            codeBuilder.appendLine("void ${declaration.name.asString()}() {")
            super.visitFunction(declaration)
            codeBuilder.appendLine("}")
        }
    }


    override fun visitCall(expression: IrCall) {
        val function = expression.symbol.owner
        val fqName = function.fqNameWhenAvailable?.asString()
        val cppFqName = functionMap[fqName] ?: fqName
        val argument = expression.arguments[0]

        val value = if (argument is IrConst) {
            val value = argument.value
            if (value is String) {
                "\"$value\""
            } else {
                value.toString()
            }
        } else if (argument is IrGetObjectValue) {

            val value = (expression.arguments[function.parameters[1].symbol.owner] as IrConst).value
            if (value is String) {
                "\"$value\""
            } else {
                value.toString()
            }
        } else {
            ""
        }
        codeBuilder.appendLine("""    $cppFqName($value);""")
        super.visitCall(expression)
    }



    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }
}

