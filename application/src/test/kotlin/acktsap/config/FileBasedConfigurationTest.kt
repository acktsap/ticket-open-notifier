package acktsap.config

import acktsap.fixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.writeLines

class FileBasedConfigurationTest {
    @TempDir
    private lateinit var tempDir: Path

    @Test
    fun includeKeywordsShouldReturnValueWhenThereIsValueInFile() {
        val targetKeywords = fixtureMonkey.giveMeOne<List<String>>()
        val includeKeywordsFile =
            tempDir.resolve(UUID.randomUUID().toString()).apply {
                writeLines(targetKeywords)
            }
        val sut =
            FileBasedConfiguration(
                includeKeywordsFile = includeKeywordsFile,
            )

        val actual = sut.includeKeywords

        actual shouldBe targetKeywords
    }

    @Test
    fun includeKeywordsShouldThrowExceptionWhenThereIsNoFile() {
        val includeKeywordsFile = tempDir.resolve(UUID.randomUUID().toString())
        val sut =
            FileBasedConfiguration(
                includeKeywordsFile = includeKeywordsFile,
            )

        shouldThrowExactly<IllegalArgumentException> {
            sut.includeKeywords
        }
    }

    @Test
    fun excludeKeywordsShouldReturnValueWhenThereIsValueInFile() {
        val targetKeywords = fixtureMonkey.giveMeOne<List<String>>()
        val excludeKeywordsFile =
            tempDir.resolve(UUID.randomUUID().toString()).apply {
                writeLines(targetKeywords)
            }
        val sut =
            FileBasedConfiguration(
                excludeKeywordsFile = excludeKeywordsFile,
            )

        val actual = sut.excludeKeywords

        actual shouldBe targetKeywords
    }

    @Test
    fun excludeKeywordsShouldThrowExceptionWhenThereIsNoFile() {
        val excludeKeywordsFile = tempDir.resolve(UUID.randomUUID().toString())
        val sut =
            FileBasedConfiguration(
                excludeKeywordsFile = excludeKeywordsFile,
            )

        shouldThrowExactly<IllegalArgumentException> {
            sut.excludeKeywords
        }
    }

    @Test
    fun configurationsShouldReturnNullWhenNoFileIsSet() {
        val sut = FileBasedConfiguration()

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
        val sut = FileBasedConfiguration()

        val actual = sut + otherConfiguration

        actual.emailSender shouldBe otherSender
    }

    @Test
    fun mergeShouldReturnConfigurationWhenNoValueIsProvidedInAnyConfiguration() {
        val otherConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val sut = FileBasedConfiguration()

        val actual = sut + otherConfiguration

        actual.emailSender shouldBe null
    }
}
