package io.github.theapache64.korduino.compiler

enum class Function(
    val fqName: (value: String?) -> String,
    val header: Header?
) {
    // Arduino
    PrintLn({ "Serial.println($it)" }, Header.Arduino),
    Begin({ "Serial.begin($it)" }, Header.Arduino),
    Delay({ "delay($it)" }, Header.Arduino),
    PinMode({ "pinMode($it)" }, Header.Arduino),

    // Std CPP
    COUT({ "std::cout << $it << std::endl" }, Header.IoStream),

    Increment({ varName -> if (varName.isNullOrBlank()) "" else "++$varName" }, null),
    Decrement({ varName -> if (varName.isNullOrBlank()) "" else "--$varName" }, null),

    PlusEquals(
        { it ->
            it.augment("+=")
        },
        null
    ),

    MinusEquals(
        { it ->
            it.augment("-=")
        },
        null
    ),
    TimesEquals(
        { it ->
            it.augment("*=")
        },
        null
    ),
    DivideEquals(
        { it ->
            it.augment("/=")
        },
        null
    ),
    ModEquals(
        { it ->
            it.augment("%=")
        },
        null
    );
}

private fun String?.augment(operator: String): String {
    val (variable, value) = this?.split(",")?.map { it.trim() } ?: listOf(null, null)
    return if (variable.isNullOrBlank() || value.isNullOrBlank()) {
        error("Invalid usage of $operator operator")
    } else {
        "$variable $operator $value"
    }
}

val functions = mapOf<String, Function>(
    // Std CPP
    "kotlin.io.println" to Function.COUT,
    "io.github.theapache64.korduino.core.Serial.println" to Function.PrintLn,
    "io.github.theapache64.korduino.core.Serial.begin" to Function.Begin,
    "io.github.theapache64.korduino.core.delay" to Function.Delay,
    "io.github.theapache64.korduino.core.pinMode" to Function.PinMode,

    // Inc/Dec
    "kotlin.Int.inc" to Function.Increment,
    "kotlin.Double.inc" to Function.Increment,
    "kotlin.Long.inc" to Function.Increment,

    "kotlin.Int.dec" to Function.Decrement,
    "kotlin.Double.dec" to Function.Decrement,
    "kotlin.Long.dec" to Function.Decrement,

    // Aug
    "kotlin.Int.plus" to Function.PlusEquals,
    "kotlin.Int.minus" to Function.MinusEquals,
    "kotlin.Int.times" to Function.TimesEquals,
    "kotlin.Int.div" to Function.DivideEquals,
    "kotlin.Int.rem" to Function.ModEquals,

    "kotlin.Double.plus" to Function.PlusEquals,
    "kotlin.Double.minus" to Function.MinusEquals,
    "kotlin.Double.times" to Function.TimesEquals,
    "kotlin.Double.div" to Function.DivideEquals,
    "kotlin.Double.rem" to Function.ModEquals,

    "kotlin.Long.plus" to Function.PlusEquals,
    "kotlin.Long.minus" to Function.MinusEquals,
    "kotlin.Long.times" to Function.TimesEquals,
    "kotlin.Long.div" to Function.DivideEquals,
    "kotlin.Long.rem" to Function.ModEquals,
)