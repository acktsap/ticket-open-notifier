package acktsap.adapters.outgoing.file

import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import acktsap.fp.IO
import acktsap.fp.Resource
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.exists

class ViewedTicketOpenFileTest : StringSpec({
    "writeProcessedTicketsShouldReplaceExistingRecordsWithoutLeavingTemporaryFiles" {
        temporaryDirectory().use { directory ->
            IO.delay {
                val ticket = TicketOpen("공연, 지역\\경로\n둘째 줄", Platform.INTERPARK, LocalDateTime.parse("2026-11-01T10:30:00"))
                val path = directory.resolve("processed.txt")
                val previous = ticket.copy(name = "old entry")
                writeProcessedTickets(path, setOf(previous)).unsafeRunSync()
                writeProcessedTickets(path, setOf(ticket)).unsafeRunSync()

                val actual = readProcessedTickets(path).unsafeRunSync()

                actual shouldBe setOf(ticket)
                Files.list(directory).use { it.count() } shouldBe 1L
            }
        }.unsafeRunSync()
    }
    "writeProcessedTicketsShouldDeferAndSnapshotItsInput" {
        temporaryDirectory().use { directory ->
            IO.delay {
                val path = directory.resolve("processed.txt")
                val ticket = TicketOpen("show", Platform.INTERPARK, LocalDateTime.of(2026, 10, 30, 12, 0))
                val tickets = mutableSetOf(ticket)
                val program = writeProcessedTickets(path, tickets)
                tickets.clear()

                path.exists() shouldBe false
                program.unsafeRunSync()
                val actual = readProcessedTickets(path).unsafeRunSync()

                actual shouldBe setOf(ticket)
                Files.list(directory).use { it.count() } shouldBe 1L
            }
        }.unsafeRunSync()
    }

    "writeProcessedTicketsShouldLeaveTheTargetAndRemoveTemporaryFilesWhenReplacementFails" {
        temporaryDirectory().use { directory ->
            IO.delay {
                val target = Files.createDirectory(directory.resolve("processed"))
                val sentinel = Files.writeString(target.resolve("existing"), "keep")

                val actual = writeProcessedTickets(target, emptySet()).attempt().unsafeRunSync()

                actual.isLeft() shouldBe true
                Files.readString(sentinel) shouldBe "keep"
                Files.list(directory).use { it.count() } shouldBe 1L
            }
        }.unsafeRunSync()
    }
})

private fun temporaryDirectory(): Resource<Path> =
    Resource.make(IO.delay { Files.createTempDirectory("ticket-open-notifier") }) { directory ->
        IO.delay<Unit> { directory.toFile().deleteRecursively() }
    }
