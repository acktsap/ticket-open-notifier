package acktsap.bootstrap

import acktsap.adapters.incoming.cli.PrepareNotification
import acktsap.application.NotificationPorts
import acktsap.application.NotificationSettings
import acktsap.configuration.LoadSettings
import acktsap.configuration.MissingSettings
import acktsap.configuration.SettingsLayer
import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import acktsap.fp.IO
import acktsap.fp.left
import acktsap.fp.right
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import java.time.LocalDateTime

class PrepareNotificationTest : StringSpec({
    "prepareNotificationShouldDeferConfigurationAndStopBeforeBuildingPortsWhenCoreSettingsAreMissing" {
        val missing = MissingSettings(listOf("visited file path"))
        val overrides = SettingsLayer(includeKeywords = listOf("musical"))
        val loaded = mutableListOf<SettingsLayer>()
        val loadSettings: LoadSettings = { value ->
            IO.delay {
                loaded.add(value)
                missing.left()
            }
        }
        val prepare: PrepareNotification = prepareNotification(loadSettings) { error("Ports must not be configured") }
        val program = prepare(overrides)

        loaded shouldBe emptyList()
        val actual = program.unsafeRunSync()

        actual shouldBe missing.left()
        loaded shouldBe listOf(overrides)
    }

    "prepareNotificationShouldStopWhenARegisteredHandlerCannotBeConfigured" {
        val settings = NotificationSettings(emptySet(), emptySet(), Path.of("visited.txt"))
        val missing = MissingSettings(listOf("email sender", "email password"))
        val loadSettings: LoadSettings = { IO.pure(settings.right()) }
        val prepare: PrepareNotification = prepareNotification(loadSettings) { IO.pure(missing.left()) }

        val actual = prepare(SettingsLayer()).unsafeRunSync()

        actual shouldBe missing.left()
    }

    "prepareNotificationShouldPrepareANonMailUseCaseWithoutRunningIt" {
        val now = LocalDateTime.of(2026, 10, 30, 12, 0)
        val previous = setOf(TicketOpen("processed", Platform.INTERPARK, now))
        val settings = NotificationSettings(emptySet(), emptySet(), Path.of("visited.txt"))
        val assembled = mutableListOf<NotificationSettings>()
        val reads = mutableListOf<Path>()
        val writes = mutableListOf<Pair<Path, Set<TicketOpen>>>()
        val loadSettings: LoadSettings = { IO.pure(settings.right()) }
        val prepare: PrepareNotification =
            prepareNotification(loadSettings) { resolved ->
                IO.delay {
                    assembled.add(resolved)
                    NotificationPorts(
                        read = { path ->
                            IO.delay {
                                reads.add(path)
                                previous
                            }
                        },
                        detectors = emptyList(),
                        handlers = emptyList(),
                        write = { path, tickets -> IO.delay<Unit> { writes.add(path to tickets) } },
                        now = IO.pure(now),
                        log = { IO.pure(Unit) },
                    ).right()
                }
            }

        val preparation = prepare(SettingsLayer())

        assembled shouldBe emptyList()
        val prepared = preparation.unsafeRunSync()
        assembled shouldBe listOf(settings)
        reads shouldBe emptyList()
        writes shouldBe emptyList()

        val notifyTickets = prepared.fold({ error("Unexpected configuration error: $it") }, { it })
        val execution = notifyTickets()
        reads shouldBe emptyList()
        execution.unsafeRunSync() shouldBe Unit

        reads shouldBe listOf(settings.visitedFilePath)
        writes shouldBe listOf(settings.visitedFilePath to previous)
    }
})
