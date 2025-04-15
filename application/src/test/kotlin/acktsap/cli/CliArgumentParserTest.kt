package acktsap.cli

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.UUID

class CliArgumentParserTest {
    @Test
    fun parseShouldParseFilePathExactly() {
        val filePath = UUID.randomUUID().toString()
        val args = arrayOf("-p", filePath)
        val sut = CliArgumentParser()

        val configuration = sut.parse(args)

        configuration.visitedFilePath shouldBe Path.of(filePath)
    }

    @Test
    fun parseShouldThrowExceptionWhenThereIsNoFileParameter() {
        val args = arrayOf("-p")
        val sut = CliArgumentParser()

        shouldThrowExactly<IllegalArgumentException> {
            sut.parse(args)
        }
    }

    @Test
    fun parseShouldThrowUnsupportedOperationException() {
        val args = arrayOf("-h")
        val sut = CliArgumentParser()

        shouldThrowExactly<UnsupportedOperationException> {
            sut.parse(args)
        }
    }
}
