package io.github.theapache64.korduino.core

object Serial {
    external fun begin(baudRate: Int) // TODO: Support enum version as well
    external fun println(string: String)
}