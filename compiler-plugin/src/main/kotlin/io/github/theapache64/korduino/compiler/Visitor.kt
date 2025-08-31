package io.github.theapache64.korduino.compiler

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid


enum class Function(
    val fqName: (value: String?) -> String,
    val header: Header
) {
    // Arduino
    PrintLn({ "Serial.println(\"$it\")" }, Header.Arduino),
    Begin({ "Serial.begin($it)" }, Header.Arduino),
    Delay({ "delay($it)" }, Header.Arduino),

    // Std CPP
    COUT({ "std::cout << \"$it\" << std::endl" }, Header.IoStream)
}

private val commonDataTypes = mapOf(
    "Int" to "int",
    "Unit" to "void",
)

private val arduinoDataTypes = mapOf<String, String>(

)

private val dataTypes = commonDataTypes + arduinoDataTypes

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

    companion object {
        const val LINK_GITHUB_ISSUES = "https://github.com/theapache64/korduino/issues/new"
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
                ?: error("Unsupported data type '$dataTypeClassName' (platform: $platform) . Please raise a ticket here if you think its a framework miss -> $LINK_GITHUB_ISSUES ")
            codeBuilder.appendLine("$returnType ${declaration.name.asString()}() {")
            super.visitFunction(declaration)
            codeBuilder.appendLine("}")
        }
    }


    override fun visitCall(expression: IrCall) {
        val function = expression.symbol.owner
        val fqName = function.fqNameWhenAvailable?.asString()
        val cppFqName = functions[fqName]
            ?: error("Unsupported function name '$fqName' (platform: $platform) . Please raise a ticket here if you think its a framework miss -> $LINK_GITHUB_ISSUES ")
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

            else -> {
                ""
            }
        }
        codeBuilder.appendLine("""    ${cppFqName.fqName(value)};""")

        // Check if header is present
        if (!codeBuilder.contains(cppFqName.header)) {
            codeBuilder.add(cppFqName.header)
        }

        super.visitCall(expression)
    }


    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }
}

