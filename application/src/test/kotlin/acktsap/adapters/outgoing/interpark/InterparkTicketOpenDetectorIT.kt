package acktsap.adapters.outgoing.interpark

import acktsap.adapters.incoming.cli.Console
import acktsap.adapters.incoming.cli.applicationProgram
import acktsap.adapters.outgoing.file.readProcessedTickets
import acktsap.adapters.outgoing.file.writeProcessedTickets
import acktsap.adapters.outgoing.gmail.EmailContent
import acktsap.adapters.outgoing.gmail.renderEmail
import acktsap.application.NotificationPorts
import acktsap.bootstrap.prepareNotification
import acktsap.configuration.resolveSettings
import acktsap.domain.retainRecentTickets
import acktsap.fp.IO
import acktsap.fp.Resource
import acktsap.fp.right
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.minutes

class InterparkTicketOpenDetectorIT : StringSpec({
    "liveDetectionShouldFeedTheCliUseCaseAndPersistWithoutSendingMail".config(
        enabled = System.getenv("TICKET_NOTIFIER_LIVE_TEST") == "true",
        timeout = 5.minutes,
    ) {
        Resource.make(IO.delay { Files.createTempDirectory("ticket-live-test") }) { directory ->
            IO.delay<Unit> { directory.toFile().deleteRecursively() }
        }.use { directory ->
            detectInterparkTicketOpens().flatMap { detected ->
                detected.shouldNotBeEmpty()
                detected.all { it.name.isNotBlank() } shouldBe true
                detected.distinct() shouldBe detected
                val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                val expectedStored = retainRecentTickets(detected.toSet(), now.minusDays(7))
                val rendered = mutableListOf<EmailContent>()
                val path = directory.resolve("processed.txt")
                val prepare =
                    prepareNotification(
                        loadSettings = { overrides -> IO.pure(resolveSettings(listOf(overrides))) },
                        portsFor = {
                            IO.pure(
                                NotificationPorts(
                                    read = ::readProcessedTickets,
                                    detectors = listOf({ IO.pure(detected) }),
                                    handlers = listOf({ plan -> IO.delay<Unit> { rendered.add(renderEmail(plan.selected)) } }),
                                    write = ::writeProcessedTickets,
                                    now = IO.pure(now),
                                    log = { IO.pure(Unit) },
                                ).right(),
                            )
                        },
                    )
                val console = Console(printLine = { IO.pure(Unit) }, printError = { IO.pure(Unit) })
                val program = applicationProgram(listOf("-p", path.toString()), console, prepare)
                program.flatMap { result ->
                    result shouldBe Unit.right()
                    rendered shouldBe listOf(renderEmail(detected))
                    readProcessedTickets(path)
                }.flatMap { stored ->
                    stored shouldBe expectedStored
                    program
                }.flatMap { repeated ->
                    repeated shouldBe Unit.right()
                    val remaining = detected.filterNot { it in expectedStored }
                    val expectedEmails =
                        listOf(renderEmail(detected)) +
                            if (remaining.isEmpty()) emptyList() else listOf(renderEmail(remaining))
                    rendered shouldBe expectedEmails
                    readProcessedTickets(path)
                }.map { stored -> stored shouldBe expectedStored }
            }
        }.unsafeRunSync()
    }
})
