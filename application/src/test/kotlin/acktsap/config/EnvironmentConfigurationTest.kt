package acktsap.config

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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
        sut.includeKeywords shouldBe null
        sut.excludeKeywords shouldBe null
        sut.emailSender shouldBe null
        sut.emailSenderPassword shouldBe null
        sut.emailRecipients shouldBe null
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
        actual.emailSender shouldBe otherSender
    }

    @Test
    fun merge_bothNotExists_returnNull() {
        // given
        val otherConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val sut = EnvironmentConfiguration()

        // when
        val actual = sut + otherConfiguration

        // then
        actual.emailSender shouldBe null
    }
}
