package io.github.theapache64.korduino.core

enum class PinMode {
    INPUT,
    OUTPUT
}

external fun delay(timeInMs: Long)
external fun pinMode(pin: Pin, mode: PinMode)
external fun pinMode(gpio: Int, mode: PinMode)
external fun analogRead(gpio: Int) : Int