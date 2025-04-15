package acktsap.config

import acktsap.fixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class EnvironmentConfigurationTest {
    @Test
    fun configurationsShouldReturnNullForNonExistingOne() {
        val sut = EnvironmentConfiguration()

        sut.includeKeywords shouldBe null
        sut.excludeKeywords shouldBe null
        sut.emailSender shouldBe null
        sut.emailSenderPassword shouldBe null
        sut.emailRecipients shouldBe null
    }

    @Test
    fun mergeShouldReturnConfigurationWithExistingOne() {
        val otherSender = fixtureMonkey.giveMeOne<String>()
        val otherConfiguration = mockk<Configuration> { every { emailSender } returns otherSender }
        val sut = EnvironmentConfiguration()

        val actual = sut + otherConfiguration

        actual.emailSender shouldBe otherSender
    }

    @Test
    fun mergeShouldReturnConfigurationWhenNoValueIsProvidedInAnyConfiguration() {
        val otherConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val sut = EnvironmentConfiguration()

        val actual = sut + otherConfiguration

        actual.emailSender shouldBe null
    }
}
