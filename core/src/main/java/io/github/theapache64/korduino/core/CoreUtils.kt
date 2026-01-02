package io.github.theapache64.korduino.core

enum class PinMode {
    INPUT,
    OUTPUT
}

enum class Digital {
    HIGH, LOW
}

external fun delay(timeInMs: Long)
external fun pinMode(pin: Pin, mode: PinMode)
external fun pinMode(gpio: Int, mode: PinMode)
external fun analogRead(gpio: Int) : Int
external fun analogRead(pin: Pin): Int
external fun digitalWrite(gpio: Int, value: Digital)