plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    group = "io.github.theapache64.korduino"
    version = property("korduino.version") as String
}
