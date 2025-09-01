import io.github.theapache64.korduino.core.Pin
import io.github.theapache64.korduino.core.PinMode
import io.github.theapache64.korduino.core.Serial
import io.github.theapache64.korduino.core.delay
import io.github.theapache64.korduino.core.pinMode

fun setup() {
    Serial.begin(115200);
}

fun loop() {
    pinMode(Pin.D1, PinMode.INPUT)
    pinMode(32, PinMode.INPUT)
}