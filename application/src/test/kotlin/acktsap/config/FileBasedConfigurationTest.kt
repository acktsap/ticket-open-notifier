package acktsap.config

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
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
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @TempDir
    private lateinit var tempDir: Path

    @Test
    fun includeKeywords() {
        // given
        val targetKeywords = fixtureMonkey.giveMeOne<List<String>>()
        val includeKeywordsFile =
            tempDir.resolve(UUID.randomUUID().toString()).apply {
                writeLines(targetKeywords)
            }
        val sut =
            FileBasedConfiguration(
                includeKeywordsFile = includeKeywordsFile,
            )

        // when
        val actual = sut.includeKeywords

        // then
        actual shouldBe targetKeywords
    }

    @Test
    fun includeKeywords_notExistsFile_throwException() {
        // given
        val includeKeywordsFile = tempDir.resolve(UUID.randomUUID().toString())
        val sut =
            FileBasedConfiguration(
                includeKeywordsFile = includeKeywordsFile,
            )

        // when, then
        shouldThrowExactly<IllegalArgumentException> {
            sut.includeKeywords
        }
    }

    @Test
    fun excludeKeywords() {
        // given
        val targetKeywords = fixtureMonkey.giveMeOne<List<String>>()
        val excludeKeywordsFile =
            tempDir.resolve(UUID.randomUUID().toString()).apply {
                writeLines(targetKeywords)
            }
        val sut =
            FileBasedConfiguration(
                excludeKeywordsFile = excludeKeywordsFile,
            )

        // when
        val actual = sut.excludeKeywords

        // then
        actual shouldBe targetKeywords
    }

    @Test
    fun excludeKeywords_notExistsFile_returnNull() {
        // given
        val excludeKeywordsFile = tempDir.resolve(UUID.randomUUID().toString())
        val sut =
            FileBasedConfiguration(
                excludeKeywordsFile = excludeKeywordsFile,
            )

        // when, then
        shouldThrowExactly<IllegalArgumentException> {
            sut.excludeKeywords
        }
    }

    @Test
    fun configurations_notSetAnything_returnNullForAll() {
        // given
        val sut = FileBasedConfiguration()

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
        val sut = FileBasedConfiguration()

        // when
        val actual = sut + otherConfiguration

        // then
        actual.emailSender shouldBe otherSender
    }

    @Test
    fun merge_bothNotExists_returnNull() {
        // given
        val otherConfiguration = mockk<Configuration> { every { emailSender } returns null }
        val sut = FileBasedConfiguration()

        // when
        val actual = sut + otherConfiguration

        // then
        actual.emailSender shouldBe null
    }
}
