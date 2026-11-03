package acktsap.adapters.outgoing.file

import acktsap.domain.TicketOpen
import acktsap.fp.IO
import acktsap.fp.Resource
import kotlinx.collections.immutable.toPersistentSet
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

fun readProcessedTickets(path: Path): IO<Set<TicketOpen>> =
    IO.delay {
        if (path.exists()) {
            path.readLines().map(::deserializeTicketOpen).toSet()
        } else {
            emptySet()
        }
    }

/** Requires atomic replacement support on the target filesystem; unsupported filesystems fail without a fallback. */
fun writeProcessedTickets(
    path: Path,
    tickets: Set<TicketOpen>,
): IO<Unit> {
    val snapshot = tickets.toPersistentSet()
    return IO.defer {
        val target = path.toAbsolutePath()
        val records = snapshot.map(::serializeTicketOpen)
        IO.delay { target.parent.createDirectories() }.then(
            Resource.make(
                acquire = IO.delay { Files.createTempFile(target.parent, ".processed-", ".tmp") },
                release = { temporary -> IO.delay<Unit> { Files.deleteIfExists(temporary) } },
            ).use { temporary ->
                IO.delay<Unit> {
                    temporary.writeLines(records)
                    Files.move(temporary, target, ATOMIC_MOVE, REPLACE_EXISTING)
                }
            },
        )
    }
}
