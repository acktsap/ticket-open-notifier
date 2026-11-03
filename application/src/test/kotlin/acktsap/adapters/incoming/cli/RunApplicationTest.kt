package acktsap.adapters.incoming.cli

import acktsap.configuration.MissingSettings
import acktsap.fp.IO
import acktsap.fp.left
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class RunApplicationTest : StringSpec({
    "runApplicationShouldDeferHelpOutputAndWriteOnlyToStandardOutput" {
        val output = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val console =
            Console(
                printLine = { text -> IO.delay<Unit> { output.add(text) } },
                printError = { text -> IO.delay<Unit> { errors.add(text) } },
            )
        val program = runApplication(applicationProgram(listOf("-h"), console, { error("Use case must not run") }), console)

        output shouldBe emptyList()
        errors shouldBe emptyList()
        val actual = program.unsafeRunSync()

        actual shouldBe 0
        output shouldBe listOf(commandLineHelp())
        errors shouldBe emptyList()
    }

    "runApplicationShouldReportCommandLineFailureToStandardError" {
        val option = "--${UUID.randomUUID()}"
        val output = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val console =
            Console(
                printLine = { text -> IO.delay<Unit> { output.add(text) } },
                printError = { text -> IO.delay<Unit> { errors.add(text) } },
            )

        val actual =
            runApplication(
                applicationProgram(listOf(option), console, { error("Use case must not run") }),
                console,
            ).unsafeRunSync()

        actual shouldBe 1
        output shouldBe emptyList()
        errors shouldBe listOf("Unknown option: $option")
    }

    "runApplicationShouldReportEveryMissingSettingWithoutExecutingLiveAdapters" {
        val fields = listOf("email sender", "visited file path")
        val output = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val console =
            Console(
                printLine = { text -> IO.delay<Unit> { output.add(text) } },
                printError = { text -> IO.delay<Unit> { errors.add(text) } },
            )
        val program = IO.pure(ApplicationError.InvalidSettings(MissingSettings(fields)).left())

        val actual = runApplication(program, console).unsafeRunSync()

        actual shouldBe 1
        output shouldBe emptyList()
        errors shouldBe listOf("Missing required settings: ${fields.joinToString()}")
    }

    "runApplicationShouldReportExecutionFailureToStandardError" {
        val failure = IllegalStateException(UUID.randomUUID().toString())
        val output = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val console =
            Console(
                printLine = { text -> IO.delay<Unit> { output.add(text) } },
                printError = { text -> IO.delay<Unit> { errors.add(text) } },
            )

        val actual = runApplication(IO.raiseError(failure), console).unsafeRunSync()

        actual shouldBe 1
        output shouldBe emptyList()
        errors shouldBe listOf(failure.message)
    }
})
