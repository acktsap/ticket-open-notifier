package acktsap.detector

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InterparkTicketOpenDetectorIT {
    @Test
    fun detect() {
        // given
        val sut = InterparkTicketOpenDetector()

        // when
        val actual = sut.detect()

        // then
        assertThat(actual).isNotEmpty
        actual.forEach { println(it) }
    }
}
