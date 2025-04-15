package acktsap.config

import acktsap.fixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class CompositeConfigurationTest {
    @Test
    fun includeKeywordsShouldReturnExistingFirstOne() {
        val mockTargetKeywords = fixtureMonkey.giveMeOne<List<String>>()
        val anotherMockTargetKeywords = fixtureMonkey.giveMeOne<List<String>>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { includeKeywords } returns null },
                mockk<Configuration> { every { includeKeywords } returns mockTargetKeywords },
                mockk<Configuration> { every { includeKeywords } returns anotherMockTargetKeywords },
            )

        val actual = sut.includeKeywords

        actual shouldBe mockTargetKeywords
    }

    @Test
    fun excludeKeywordsShouldReturnExistingFirstOne() {
        val mockTargetKeywords = fixtureMonkey.giveMeOne<List<String>>()
        val anotherMockTargetKeywords = fixtureMonkey.giveMeOne<List<String>>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { excludeKeywords } returns null },
                mockk<Configuration> { every { excludeKeywords } returns mockTargetKeywords },
                mockk<Configuration> { every { excludeKeywords } returns anotherMockTargetKeywords },
            )

        val actual = sut.excludeKeywords

        actual shouldBe mockTargetKeywords
    }

    @Test
    fun emailSenderShouldReturnExistingFirstOne() {
        val mockEmailSender = fixtureMonkey.giveMeOne<String>()
        val anotherMockEmailSender = fixtureMonkey.giveMeOne<String>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { emailSender } returns null },
                mockk<Configuration> { every { emailSender } returns mockEmailSender },
                mockk<Configuration> { every { emailSender } returns anotherMockEmailSender },
            )

        val actual = sut.emailSender

        actual shouldBe mockEmailSender
    }

    @Test
    fun emailSenderPasswordShouldReturnExistingFirstOne() {
        val mockEmailSenderPassword = fixtureMonkey.giveMeOne<String>()
        val anotherMockEmailSenderPassword = fixtureMonkey.giveMeOne<String>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { emailSenderPassword } returns null },
                mockk<Configuration> { every { emailSenderPassword } returns mockEmailSenderPassword },
                mockk<Configuration> { every { emailSenderPassword } returns anotherMockEmailSenderPassword },
            )

        val actual = sut.emailSenderPassword

        actual shouldBe mockEmailSenderPassword
    }

    @Test
    fun emailRecipientsShouldReturnExistingFirstOne() {
        val mockEmailRecipients = fixtureMonkey.giveMeOne<List<String>>()
        val anotherMockEmailRecipients = fixtureMonkey.giveMeOne<List<String>>()
        val sut =
            CompositeConfiguration(
                mockk<Configuration> { every { emailRecipients } returns null },
                mockk<Configuration> { every { emailRecipients } returns mockEmailRecipients },
                mockk<Configuration> { every { emailRecipients } returns anotherMockEmailRecipients },
            )

        val actual = sut.emailRecipients

        actual shouldBe mockEmailRecipients
    }

    @Test
    fun mergeShouldTakeFirstOneWhenBothExists() {
        val firstSender = fixtureMonkey.giveMeOne<String>()
        val secondSender = fixtureMonkey.giveMeOne<String>()
        val firstConfiguration = mockk<Configuration> { every { emailSender } returns firstSender }
        val secondConfiguration = mockk<Configuration> { every { emailSender } returns secondSender }
        val sut = CompositeConfiguration(firstConfiguration)

        val actual = sut + secondConfiguration

        actual.emailSender shouldBe firstSender
    }

    @Test
    fun mergeShouldTakeFirstOneWhenNoSecondItem() {
        val firstSender = fixtureMonkey.giveMeOne<String>()
        val firstConfiguration = mockk<Configuration> { every { emailSender } returns firstSender }
        val secondConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val sut = CompositeConfiguration(firstConfiguration)

        val actual = sut + secondConfiguration

        actual.emailSender shouldBe firstSender
    }

    @Test
    fun mergeShouldReturnExistingOneWhenNoFirstItem() {
        val secondSender = fixtureMonkey.giveMeOne<String>()
        val firstConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val secondConfiguration = mockk<Configuration> { every { emailSender } returns secondSender }
        val sut = CompositeConfiguration(firstConfiguration)

        val actual = sut + secondConfiguration

        actual.emailSender shouldBe secondSender
    }

    @Test
    fun mergeShouldReturnNullWhenBothNotExists() {
        val firstConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val secondConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val sut = CompositeConfiguration(firstConfiguration)

        val actual = sut + secondConfiguration

        actual.emailSender shouldBe null
    }
}
