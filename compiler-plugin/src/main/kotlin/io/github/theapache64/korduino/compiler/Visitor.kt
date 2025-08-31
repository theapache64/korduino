package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid


enum class Function(
    val fqName: (value: String?) -> String,
    val header: Header
) {
    // Arduino
    PrintLn({ "Serial.println($it)" }, Header.Arduino),
    Begin({ "Serial.begin($it)" }, Header.Arduino),
    Delay({ "delay($it)" }, Header.Arduino),

    // Std CPP
    COUT({ "std::cout << \"$it\" << std::endl;" }, Header.IoStream)
}

private val arduinoFunctions = mapOf<String, Function>(
    "kotlin.io.println" to Function.PrintLn,
    "io.github.theapache64.korduino.core.Serial.println" to Function.PrintLn,
    "io.github.theapache64.korduino.core.Serial.begin" to Function.Begin,
    "io.github.theapache64.korduino.core.delay" to Function.Delay
)

private val stdCppFunctions = mapOf<String, Function>(
    "kotlin.io.println" to Function.COUT
)

class Visitor(
    private val platform: Arg.Mode.Platform
) : IrVisitorVoid() {

    private val functions = when (platform) {
        Arg.Mode.Platform.ARDUINO -> arduinoFunctions
        Arg.Mode.Platform.STD_CPP -> stdCppFunctions
    }

    private val codeBuilder = StringBuilder()
    fun generateCode(): String = codeBuilder.toString()

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
        val cppFqName = functions[fqName]
            ?: error("Unsupported function name '$fqName' (platform: $platform) . Please raise a ticket here if you think its a framework miss -> https://github.com/theapache64/korduino/issues ")
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
        codeBuilder.appendLine("""    ${cppFqName.fqName(value)};""")

        // Check if header is present
        if(!codeBuilder.contains(cppFqName.header)){
            codeBuilder.add(cppFqName.header)
        }

        super.visitCall(expression)
    }


    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }
}

