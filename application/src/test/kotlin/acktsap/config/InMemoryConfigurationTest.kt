package acktsap.config

import acktsap.fixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class InMemoryConfigurationTest {
    @Test
    fun configurationsShouldReturnValueWhenItsAllSet() {
        val includeKeywords = fixtureMonkey.giveMeOne<List<String>?>()
        val excludeKeywords = fixtureMonkey.giveMeOne<List<String>?>()
        val emailSender = fixtureMonkey.giveMeOne<String>()
        val emailSenderPassword = fixtureMonkey.giveMeOne<String>()
        val emailRecipients = fixtureMonkey.giveMeOne<List<String>?>()
        val sut =
            InMemoryConfiguration(
                includeKeywords = includeKeywords,
                excludeKeywords = excludeKeywords,
                emailSender = emailSender,
                emailSenderPassword = emailSenderPassword,
                emailRecipients = emailRecipients,
            )

        sut.includeKeywords shouldBe includeKeywords
        sut.excludeKeywords shouldBe excludeKeywords
        sut.emailSender shouldBe emailSender
        sut.emailSenderPassword shouldBe emailSenderPassword
        sut.emailRecipients shouldBe emailRecipients
    }

    @Test
    fun mergeShouldConfigurationWithFirstOneWhenBothExists() {
        val firstSender = fixtureMonkey.giveMeOne<String>()
        val secondSender = fixtureMonkey.giveMeOne<String>()
        val secondConfiguration = mockk<Configuration> { every { emailSender } returns secondSender }
        val sut = InMemoryConfiguration(emailSender = firstSender)

        val actual = sut + secondConfiguration

        actual.emailSender shouldBe firstSender
    }

    @Test
    fun mergeShouldReturnConfigurationWithExistingOne() {
        val secondSender = fixtureMonkey.giveMeOne<String>()
        val secondConfiguration = mockk<Configuration> { every { emailSender } returns secondSender }
        val sut = InMemoryConfiguration(emailSender = null)

        val actual = sut + secondConfiguration

        actual.emailSender shouldBe secondSender
    }

    @Test
    fun mergeShouldReturnConfigurationWhenNoValueIsProvidedInAnyConfiguration() {
        val secondConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val sut = InMemoryConfiguration(emailSender = null)

        val actual = sut + secondConfiguration

        actual.emailSender shouldBe null
    }
}
