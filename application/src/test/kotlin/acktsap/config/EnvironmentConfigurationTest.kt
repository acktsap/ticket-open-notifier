package acktsap.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnvironmentConfigurationTest {
    @Test
    fun configurations_notExists_returnNullOrEmpty() {
        // given
        val sut = EnvironmentConfiguration()

        // when, then
        assertThat(sut.targetKeywords).isNull()
        assertThat(sut.emailSender).isNull()
        assertThat(sut.emailSenderPassword).isNull()
        assertThat(sut.emailRecipients).isNull()
    }
}
