package acktsap.config

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CompositeConfigurationTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun targetKeywords() {
        // given
        val mockTargetKeywords = fixtureMonkey.giveMeOne<List<String>?>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { targetKeywords } returns mockTargetKeywords },
            )

        // when
        val actual = sut.targetKeywords

        // then
        assertThat(actual).isEqualTo(mockTargetKeywords)
    }

    @Test
    fun emailSender() {
        // given
        val mockEmailSender = fixtureMonkey.giveMeOne<String?>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { emailSender } returns mockEmailSender },
            )

        // when
        val actual = sut.emailSender

        // then
        assertThat(actual).isEqualTo(mockEmailSender)
    }

    @Test
    fun emailSenderPassword() {
        // given
        val mockEmailSenderPassword = fixtureMonkey.giveMeOne<String?>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { emailSenderPassword } returns mockEmailSenderPassword },
            )

        // when
        val actual = sut.emailSenderPassword

        // then
        assertThat(actual).isEqualTo(mockEmailSenderPassword)
    }

    @Test
    fun emailRecipients() {
        // given
        val mockEmailRecipients = fixtureMonkey.giveMeOne<List<String>?>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { emailRecipients } returns mockEmailRecipients },
            )

        // when
        val actual = sut.emailRecipients

        // then
        assertThat(actual).isEqualTo(mockEmailRecipients)
    }
}
