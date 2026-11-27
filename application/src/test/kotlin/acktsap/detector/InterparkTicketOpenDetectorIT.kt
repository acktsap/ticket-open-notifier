package acktsap.detector

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class InterparkTicketOpenDetectorIT {
    @Disabled("Manual test")
    @Test
    fun detect() {
        val sut = InterparkTicketOpenDetector()

        val actual = sut.detect()

        println("Detected ${actual.size} tickets:")
        actual.forEach { println(" - $it") }
        assert(actual.isNotEmpty())
    }
}
