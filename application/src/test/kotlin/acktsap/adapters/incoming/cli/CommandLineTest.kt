package acktsap.adapters.incoming.cli

import acktsap.configuration.SettingsLayer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.nio.file.Path

class CommandLineTest : StringSpec({
    "parseCommandLineShouldReturnRunForEveryPrintablePath" {
        checkAll(Arb.string(1..80)) { pathText ->
            val path = Path.of(pathText)

            parseCommandLine(listOf("-p", pathText)) shouldBe CommandLineResult.Run(SettingsLayer(visitedFilePath = path))
        }
    }

    "parseCommandLineShouldReturnHelpWhenHelpFlagIsProvided" {
        parseCommandLine(listOf("-h")) shouldBe CommandLineResult.Help
    }

    "parseCommandLineShouldReturnInvalidWhenPathValueIsMissing" {
        parseCommandLine(listOf("-p")) shouldBe CommandLineResult.Invalid("No file path provided after -p")
    }

    "parseCommandLineShouldRejectUnknownOptions" {
        parseCommandLine(listOf("-p", "processed.txt", "--typo")) shouldBe
            CommandLineResult.Invalid("Unknown option: --typo")
    }
    "parseCommandLineShouldUseTheLastPathWhenPathOptionIsRepeated" {
        checkAll(Arb.list(Arb.string(1..40), 1..30)) { paths ->
            val arguments = paths.flatMap { listOf("-p", it) }

            val actual = parseCommandLine(arguments)

            actual shouldBe CommandLineResult.Run(SettingsLayer(visitedFilePath = Path.of(paths.last())))
        }
    }

    "parseCommandLineShouldKeepHelpAndErrorsTerminalForAnyRemainingArguments" {
        checkAll(Arb.list(Arb.string(0..40), 0..30)) { remaining ->
            val help = parseCommandLine(listOf("-h") + remaining)
            val invalid = parseCommandLine(listOf("--unknown") + remaining)

            help shouldBe CommandLineResult.Help
            invalid shouldBe CommandLineResult.Invalid("Unknown option: --unknown")
        }
    }

    "parseCommandLineShouldRejectAnInvalidPathWithoutThrowing" {
        val actual = parseCommandLine(listOf("-p", "invalid\u0000path"))

        actual.shouldBeInstanceOf<CommandLineResult.Invalid>()
    }

    "parseCommandLineShouldRequireAPathWhenArgumentsAreEmpty" {
        val actual = parseCommandLine(emptyList())

        actual.shouldBeInstanceOf<CommandLineResult.Invalid>()
    }

    "parseCommandLineShouldRejectAnUnfinishedRepeatedPathOption" {
        val actual = parseCommandLine(listOf("-p", "first", "-p"))

        actual shouldBe CommandLineResult.Invalid("No file path provided after -p")
    }

    "parseCommandLineShouldTreatTheTokenAfterPathOptionAsItsValue" {
        val actual = parseCommandLine(listOf("-p", "-h"))

        actual shouldBe CommandLineResult.Run(SettingsLayer(visitedFilePath = Path.of("-h")))
    }
})
