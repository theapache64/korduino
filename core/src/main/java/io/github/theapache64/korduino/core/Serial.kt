package io.github.theapache64.korduino.core

object Serial {
    external fun begin(baudRate: Int)
    external fun println(string: String)
}