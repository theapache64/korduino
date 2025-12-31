package io.github.theapache64.korduino.common

enum class Baud(
    val value: Int
) {
    RATE_50(50),
    RATE_75(75),
    RATE_110(110),
    RATE_134(134),
    RATE_150(150),
    RATE_200(200),
    RATE_300(300),
    RATE_600(600),
    RATE_1200(1200),
    RATE_1800(1800),
    RATE_2400(2400),
    RATE_4800(4800),
    RATE_9600(9600),
    RATE_19200(19200),
    RATE_28800(28800),
    RATE_38400(38400),
    RATE_57600(57600),
    RATE_76800(76800),
    RATE_115200(115200),
    RATE_230400(230400),
    RATE_460800(460800),
    RATE_576000(576000),
    RATE_921600(921600),
}