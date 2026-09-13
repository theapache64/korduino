package io.github.theapache64.korduino.compiler

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

fun <T> T.left() : Either.Left<T> {
    return Either.Left(this)
}

fun <T> T.right() : Either.Right<T> {
    return Either.Right(this)
}