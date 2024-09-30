package acktsap.config

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnvironmentConfigurationTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

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

    @Test
    fun merge_otherExists_returnOtherOne() {
        // given
        val otherSender = fixtureMonkey.giveMeOne<String?>()
        val otherConfiguration = mockk<Configuration> { every { emailSender } returns otherSender }
        val sut = EnvironmentConfiguration()

        // when
        val actual = sut + otherConfiguration

        // then
        assertThat(actual.emailSender).isEqualTo(otherSender)
    }

    @Test
    fun merge_bothNotExists_returnNull() {
        // given
        val otherConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val sut = EnvironmentConfiguration()

        // when
        val actual = sut + otherConfiguration

        // then
        assertThat(actual.emailSender).isNull()
    }
}
