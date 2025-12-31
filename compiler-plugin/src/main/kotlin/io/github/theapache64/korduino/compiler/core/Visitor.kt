package io.github.theapache64.korduino.compiler.core

import io.github.theapache64.korduino.common.Arg
import io.github.theapache64.korduino.compiler.CodeBuilder
import io.github.theapache64.korduino.compiler.DataType
import io.github.theapache64.korduino.compiler.addSemiColonIfNeeded
import io.github.theapache64.korduino.compiler.dataTypes
import io.github.theapache64.korduino.compiler.functions
import org.jetbrains.kotlin.backend.jvm.ir.getIoFile
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.ANDAND
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.EQEQEQ
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.EXCLEQ
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.EXCLEQEQ
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
import org.jetbrains.kotlin.ir.util.render
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

        const val INDEXED_ACCESS_OPERATOR = "[]"
        private val arrayRegex = "VAR name:(\\w+) type:kotlin\\.Array<([A-Za-z0-9.<>]+)> \\[val]".toRegex()
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

    override fun visitProperty(declaration: IrProperty) {
        codeBuilder.appendLine(declaration.toCodeString().joinToString(separator = " "))
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

    override fun visitConstantArray(expression: IrConstantArray) {
        super.visitConstantArray(expression)
    }

    override fun visitReturn(expression: IrReturn) {
        codeBuilder.appendLine(
            expression.toCodeString().addPreKeyword(expression).joinToString(separator = " ")
        )
    }

    override fun visitVariable(declaration: IrVariable) {
        codeBuilder.appendLine(declaration.toCodeString().joinToString(separator = " "))
    }

    override fun visitWhen(expression: IrWhen) {
        codeBuilder.appendLine(expression.toCodeString().joinToString(separator = " "))
    }


    fun parseArray(irVariable: IrVariableImpl): ArrayInfo {
        val signature = irVariable.symbol.owner.render()
        if (signature.contains("kotlin.Any")) {
            throw UnsupportedFeature("Arrays of type Any are not supported yet: `$signature`\nSee https://github.com/theapache64/korduino/issues/2")
        }
        val matchResult = arrayRegex.find(signature) ?: throw IllegalStateException("Not an array: `$signature`")
        val (variableName, dataTypeString) = matchResult.destructured
        val firstArray = (irVariable.initializer as? IrCallImpl)?.arguments?.getOrNull(0)
        val firstArrayElements = firstArray?.toCodeString()
        val variableCall = firstArrayElements?.joinToString(
            prefix = "{",
            separator = ", ",
            postfix = "}"
        ) ?: TODO()
        val dataType = dataTypes[dataTypeString] ?: throw IllegalStateException("Unknown data type: $dataTypeString")
        return ArrayInfo(
            dataType = dataType,
            size = firstArrayElements.size,
            variableName = variableName,
            variableCall = variableCall
        ).also {
            println("QuickTag: Visitor:parseArray: Parsed array info: `$it`")
        }
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(expression: IrCall) {
        val (functionCall, headers) = expression.toFunctionCall()

        if (functionCall.isNotBlank()) {
            codeBuilder.appendLine(functionCall)
        } else {
            // Couldn't figure out the function call, so visiting children to avoid missing anything
            super.visitCall(expression)
        }

        codeBuilder.addHeaders(headers)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun IrCall.toFunctionCall(): Pair<String, List<String>> {
        val function = symbol.owner
        val fqName = function.fqNameWhenAvailable?.asString()
        val argValues = mutableListOf<String>()
        for (expArg in arguments) {
            if (expArg == null) continue
            argValues.addAll(expArg.toCodeString())
        }

        val (functionCall, headers) = if (fqName == FUNCTION_RAW_CPP) {
            val code = argValues[0].let { code ->
                code.substring(1, code.lastIndex) // stripping out `"`
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


        return Pair(functionCall, headers)
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
                null // TODO: This may change in future when OOP concepts are added
            }

            is IrGetEnumValueImpl -> {
                argValues.add(symbol.owner.name.asString())
            }

            is IrVarargImpl -> {
                elements.forEach {
                    val constImpl = it as IrConstImpl
                    val varArg = if(constImpl.kind == IrConstKind.String){
                        "\"${constImpl.value}\""
                    } else {
                        "${constImpl.value}"
                    }
                    argValues.add(varArg)
                }
            }

            is IrReturnImpl -> {
                argValues.add(this.value.toCodeString().joinToString(" "))
            }

            is IrCallImpl -> {
                val isOperator = this.symbol.owner.isOperator || this.symbol.owner.origin.name == "OPERATOR"
                if (isOperator) {
                    val opName = this.symbol.owner.name.asString()
                    // TODO: use constants from IR artifact here
                    val opSymbol = when (opName) {
                        "plus" -> "+"
                        "minus" -> "-"
                        "div" -> "/"
                        "times" -> "*"
                        "rem" -> "%"
                        "greater" -> ">"
                        "greaterOrEqual" -> ">="
                        "less" -> "<"
                        "lessOrEqual" -> "<="
                        "EQEQ", "EQEQEQ", "ieee754equals" -> {
                            if (this.origin?.debugName == EXCLEQ.debugName || this.origin?.debugName == EXCLEQEQ.debugName) {
                                "!="
                            } else {
                                "=="
                            }
                        }

                        "not" -> {
                            if (this.origin?.debugName == EXCLEQ.debugName || this.origin?.debugName == EXCLEQEQ.debugName) {
                                ""
                            } else {
                                "!"
                            }
                        }

                        "get" -> {
                            INDEXED_ACCESS_OPERATOR
                        }

                        else -> error("Unknown operator `$opName`")
                    }

                    when (opSymbol) {
                        INDEXED_ACCESS_OPERATOR -> {
                            // array
                            val result = this.arguments.mapNotNull { it?.toCodeString()?.joinToString(" ") }
                            val varName = result[0]
                            val index = result[1]
                            argValues.add("$varName[$index]")
                        }

                        else -> {
                            val memAddress = if (opName == EQEQEQ.debugName) "&" else ""
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
                                        this.arguments.mapNotNull {
                                            memAddress + it?.toCodeString()?.joinToString(opSymbol)
                                        }
                                            .joinToString(" $opSymbol ")
                                    }$endBracket")
                            }
                        }
                    }
                } else {
                    val (functionCall, headers) = this.toFunctionCall()
                    argValues.addAll(listOf(functionCall))

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
                    if (typeFqName == "kotlin.Array") {
                        // Dummy: std::array<int, 5> arr = {1, 2, 3, 4, 5};
                        val arrayInfo = parseArray(this)
                        val arrayStatement =
                            "std::array<${arrayInfo.dataType.type}, ${arrayInfo.size}> ${arrayInfo.variableName} = ${arrayInfo.variableCall};"
                        println("QuickTag: Visitor:toCodeString: Parsed array statement: `$arrayStatement`")
                        argValues.add(arrayStatement)

                        codeBuilder.addHeader("array")
                    } else {
                        val variableCall = initializer?.toCodeString()?.joinToString(separator = "")
                        val dataType =
                            dataTypes[typeFqName] ?: error("couldn't find dataType for `$typeFqName` ($variableCall)")
                        if (dataType.extraHeader != null) {
                            codeBuilder.addHeader(dataType.extraHeader)
                        }
                        argValues.add("${dataType.type} $variableName = $variableCall;")
                    }
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
                                        val result =
                                            branch.result.toCodeString().addPreKeyword(branch.result).joinToString(" ")
                                                .addSemiColonIfNeeded()
                                        argValues.add(result)
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

            is IrPropertyImpl -> {
                // int a = 1;
                val constLabel = if(this.isConst) "const " else ""
                val dataType = this.backingField?.initializer?.expression?.getMappedDataType()
                val variableName = this.name
                val value = this.backingField?.initializer?.expression?.toCodeString()?.joinToString()
                argValues.add("$constLabel$dataType $variableName=$value;")
            }


            else -> error("Unhandled argValue type ${this::class.simpleName}")
        }
        return argValues.filter { it.isNotBlank() }
    }

    private fun IrExpression?.getMappedDataType() : String? {
        return when(this){
            is IrConstImpl -> when(this.kind){
                IrConstKind.Boolean -> DataType.Boolean
                IrConstKind.Double -> DataType.Double
                IrConstKind.Float -> DataType.Float
                IrConstKind.Int -> DataType.Int
                IrConstKind.Long -> DataType.Long
                IrConstKind.String -> DataType.String
                else -> error("Unsupported const kind ${this.kind}")
            }.type
            else -> error("Undefined expression `${this}`")
        }
    }

    private fun List<String>.addPreKeyword(result: IrStatement): List<String> {
        val list = this.toMutableList()
        when (result) {
            is IrReturnImpl -> {
                list.add(0, "return")
            }

            is IrBlockImpl -> {
                result.statements.forEach { statement ->
                    return list.addPreKeyword(statement)
                }
            }
        }
        return list
    }

    private fun getBrackets(
        startOffset: Int,
        endOffset: Int
    ): Pair<String, String> {
        var startBracket = ""
        var endBracket = ""
        if (isPrevCharIsBracket(startOffset) && isNextCharIsBracket(endOffset) && !isIfStatement(
                startOffset,
                endOffset
            )
        ) {
            startBracket = "("
            endBracket = ")"
        }
        return Pair(startBracket, endBracket)
    }

    private fun isIfStatement(startOffset: Int, endOffset: Int): Boolean {
        var index = startOffset - 1
        var isIfFound = false
        var isEndBracketFound = false
        while (index >= 0) {
            val char = sourceText[index]
            if (char.isWhitespace() || char == '(') {
                index--
                continue
            }
            isIfFound = sourceText.substring(index - 1, index + 1).also {
                println("QuickTag: Visitor:isIfStatement: comparing with `$it`")
            } == "if"
            break
        }

        index = endOffset
        while (index < sourceText.length) {
            val char = sourceText[index]
            if (char.isWhitespace() || char == ')') {
                index++
                continue
            }
            isEndBracketFound = char == '{'
            break
        }

        return isIfFound && isEndBracketFound
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

