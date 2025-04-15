package acktsap.detector

import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class InterparkTicketOpenDetectorIT {
    @Disabled("Manual test")
    @Test
    fun detect() {
        // given
        val sut = InterparkTicketOpenDetector()

        // when
        val actual = sut.detect()

        // then
        actual.forEach { println(it) }
    }
}
