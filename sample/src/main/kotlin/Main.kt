import io.github.theapache64.korduino.core.Serial
import io.github.theapache64.korduino.core.delay

fun setup() {
    Serial.begin(115200);
}

fun loop() {
    Serial.println("Hello SP!")
    delay(1000)
}