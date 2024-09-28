package acktsap.config

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InMemoryConfigurationTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun configurations() {
        // given
        val targetKeywords = fixtureMonkey.giveMeOne<List<String>?>()
        val emailSender = fixtureMonkey.giveMeOne<String>()
        val emailSenderPassword = fixtureMonkey.giveMeOne<String>()
        val emailRecipients = fixtureMonkey.giveMeOne<List<String>?>()
        val sut =
            InMemoryConfiguration(
                targetKeywords = targetKeywords,
                emailSender = emailSender,
                emailSenderPassword = emailSenderPassword,
                emailRecipients = emailRecipients,
            )

        // when, then
        assertThat(sut.targetKeywords).isEqualTo(targetKeywords)
        assertThat(sut.emailSender).isEqualTo(emailSender)
        assertThat(sut.emailSenderPassword).isEqualTo(emailSenderPassword)
        assertThat(sut.emailRecipients).isEqualTo(emailRecipients)
    }
}
