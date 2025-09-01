package io.github.theapache64.korduino.core

enum class PinMode {
    INPUT,
    OUTPUT
}

enum class Pin {
    D0,
    D1,
    D2,
    D3,
    D4,
    D5,
    D6,
    D7,
    D8,
    D9,
    D10,
    A0,
}

external fun delay(timeInMs: Long)
external fun pinMode(pin: Pin, mode: PinMode)
external fun pinMode(pin: Int, mode: PinMode)