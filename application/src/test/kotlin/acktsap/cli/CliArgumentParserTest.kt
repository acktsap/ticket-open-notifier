package acktsap.cli

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
        assertThat(configuration.visitedFilePath).isEqualTo(Path.of(filePath))
    }

    @Test
    fun parse_filePathNotProvided_throwException() {
        // given
        val args = arrayOf("-p")
        val sut = CliArgumentParser()

        // when, then
        assertThatThrownBy {
            sut.parse(args)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun parse_help() {
        // given
        val args = arrayOf("-h")
        val sut = CliArgumentParser()

        // when, then
        assertThatThrownBy {
            sut.parse(args)
        }.isInstanceOf(UnsupportedOperationException::class.java)
    }
}
