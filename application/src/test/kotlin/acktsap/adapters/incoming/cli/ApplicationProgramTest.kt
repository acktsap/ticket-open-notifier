package acktsap.adapters.incoming.cli

import acktsap.application.port.incoming.NotifyTickets
import acktsap.configuration.MissingSettings
import acktsap.configuration.SettingsLayer
import acktsap.fp.IO
import acktsap.fp.left
import acktsap.fp.right
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Path

class ApplicationProgramTest : StringSpec({
    "applicationProgramShouldRetainArgumentsWhenCallerMutatesTheOriginalList" {
        val arguments = mutableListOf("--unknown")
        val console = Console(printLine = { IO.pure(Unit) }, printError = { IO.pure(Unit) })
        val program = applicationProgram(arguments, console, { error("Use case must not run") })
        arguments.clear()
        arguments.add("-h")

        val actual = program.unsafeRunSync()

        actual.leftOrNull().shouldBeInstanceOf<ApplicationError.InvalidCommandLine>()
    }

    "applicationProgramShouldRepresentMissingPathAsAValue" {
        val console = Console(printLine = { IO.pure(Unit) }, printError = { IO.pure(Unit) })

        val actual = applicationProgram(emptyList(), console, { error("Use case must not run") }).unsafeRunSync()

        actual.isLeft() shouldBe true
    }
    "applicationProgramShouldPrepareWithParsedOverridesBeforeRunningTheIncomingPort" {
        val overrides = mutableListOf<SettingsLayer>()
        val console = Console(printLine = { IO.pure(Unit) }, printError = { IO.pure(Unit) })
        val events = mutableListOf<String>()
        val notifyTickets: NotifyTickets = { IO.delay<Unit> { events.add("notify") } }
        val prepare: PrepareNotification = { value ->
            IO.delay {
                overrides.add(value)
                events.add("prepare")
                notifyTickets.right()
            }
        }
        val program = applicationProgram(listOf("-p", "visited.txt"), console, prepare)

        events shouldBe emptyList()
        overrides shouldBe emptyList()
        val actual = program.unsafeRunSync()

        actual shouldBe Unit.right()
        overrides shouldBe listOf(SettingsLayer(visitedFilePath = Path.of("visited.txt")))
        events shouldBe listOf("prepare", "notify")
    }

    "applicationProgramShouldPreservePreparationFailures" {
        val missing = MissingSettings(listOf("email sender", "email password"))
        val console = Console(printLine = { IO.pure(Unit) }, printError = { IO.pure(Unit) })
        val prepare: PrepareNotification = { IO.pure(missing.left()) }

        val actual = applicationProgram(listOf("-p", "visited.txt"), console, prepare).unsafeRunSync()

        actual shouldBe ApplicationError.InvalidSettings(missing).left()
    }
})
