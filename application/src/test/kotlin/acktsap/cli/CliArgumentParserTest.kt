package acktsap.cli

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.UUID

class CliArgumentParserTest {
    @Test
    fun parse_filePath() {
        // given
        val filePath = UUID.randomUUID().toString()
        val args = arrayOf("-p", filePath)
        val sut = CliArgumentParser()

        // when
        val configuration = sut.parse(args)

        // then
        configuration.visitedFilePath shouldBe Path.of(filePath)
    }

    @Test
    fun parse_filePathNotProvided_throwException() {
        // given
        val args = arrayOf("-p")
        val sut = CliArgumentParser()

        // when, then
        shouldThrowExactly<IllegalArgumentException> {
            sut.parse(args)
        }
    }

    @Test
    fun parse_help() {
        // given
        val args = arrayOf("-h")
        val sut = CliArgumentParser()

        // when, then
        shouldThrowExactly<UnsupportedOperationException> {
            sut.parse(args)
        }
    }
}
