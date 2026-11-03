package acktsap.application.service

import acktsap.application.NotificationEnvironment
import acktsap.application.NotificationPorts
import acktsap.application.NotificationSettings
import acktsap.domain.NotificationPlan
import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import acktsap.fp.IO
import acktsap.fp.left
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.nio.file.Path
import java.time.LocalDateTime

class NotificationProgramTest : StringSpec({
    "notificationProgramShouldMergeDetectorsAndRunAllHandlersBeforePersisting" {
        checkAll(Arb.list(Arb.string(0..20), 0..10), Arb.list(Arb.string(0..20), 0..10), Arb.int(1..30)) { first, second, days ->
            val events = mutableListOf<String>()
            val now = LocalDateTime.of(2026, 10, 30, 12, 0)
            val old = TicketOpen("already", Platform.INTERPARK, now)
            val expired = old.copy(name = "expired", dateTime = now.minusDays(days.toLong()))
            val previous = setOf(old, expired)
            val firstBatch = first.map { TicketOpen("new-$it", Platform.INTERPARK, now) }
            val secondBatch = second.map { TicketOpen("new-$it", Platform.INTERPARK, now) }
            val expected = (firstBatch + secondBatch).toSet()
            val settings =
                NotificationSettings(
                    emptySet(),
                    emptySet(),
                    Path.of("viewed"),
                    days.toLong(),
                )
            val received = mutableListOf<Pair<String, NotificationPlan>>()
            val writes = mutableListOf<Pair<Path, Set<TicketOpen>>>()
            val ports =
                NotificationPorts(
                    read = {
                        IO.delay {
                            events.add("read")
                            previous
                        }
                    },
                    detectors =
                        listOf(
                            {
                                IO.delay {
                                    events.add("first detector")
                                    listOf(old) + firstBatch
                                }
                            },
                            {
                                IO.delay {
                                    events.add("second detector")
                                    listOf(old) + secondBatch + firstBatch
                                }
                            },
                        ),
                    handlers =
                        listOf(
                            { plan ->
                                IO.delay<Unit> {
                                    events.add("first handler")
                                    received.add("first" to plan)
                                }
                            },
                            { plan ->
                                IO.delay<Unit> {
                                    events.add("second handler")
                                    received.add("second" to plan)
                                }
                            },
                        ),
                    write = {
                            path,
                            tickets,
                        ->
                        IO.delay<Unit> {
                            events.add("write")
                            writes.add(path to tickets)
                        }
                    },
                    now = IO.pure(now),
                    log = { IO.pure(Unit) },
                )
            val program = notificationProgram().run(NotificationEnvironment(settings, ports))

            events shouldBe emptyList()
            program.unsafeRunSync()

            val expectedHandlers = if (expected.isEmpty()) emptyList() else listOf("first handler", "second handler")
            events shouldBe listOf("read", "first detector", "second detector") + expectedHandlers + "write"
            received.map { it.first } shouldBe if (expected.isEmpty()) emptyList() else listOf("first", "second")
            val expectedPlans = received.map { setOf(old) + expected to expected }
            received.map { (_, plan) -> plan.detected.toSet() to plan.selected.toSet() } shouldBe expectedPlans
            received.map { (_, plan) -> plan.detected.size to plan.selected.size } shouldBe
                received.map { 1 + expected.size to expected.size }
            received.map { it.second }.distinct().size shouldBe if (expected.isEmpty()) 0 else 1
            writes shouldBe listOf(settings.visitedFilePath to (setOf(old) + expected))
        }
    }

    "notificationProgramShouldStopAfterDetectorFailureWithoutHandlingPartialResults" {
        val failure = IllegalStateException()
        val events = mutableListOf<String>()
        val now = LocalDateTime.of(2026, 10, 30, 12, 0)
        val ticket = TicketOpen("show", Platform.INTERPARK, now)
        val settings = NotificationSettings(emptySet(), emptySet(), Path.of("viewed"))
        val ports =
            NotificationPorts(
                read = { IO.pure(emptySet()) },
                detectors =
                    listOf(
                        {
                            IO.delay {
                                events.add("first detector")
                                listOf(ticket)
                            }
                        },
                        {
                            IO.delay {
                                events.add("failed detector")
                                throw failure
                            }
                        },
                        {
                            IO.delay {
                                events.add("skipped detector")
                                emptyList()
                            }
                        },
                    ),
                handlers = listOf({ IO.delay<Unit> { events.add("handler") } }),
                write = { _, _ -> IO.delay<Unit> { events.add("write") } },
                now = IO.pure(now),
                log = { IO.pure(Unit) },
            )

        val actual = notificationProgram().run(NotificationEnvironment(settings, ports)).attempt().unsafeRunSync()

        actual shouldBe failure.left()
        events shouldBe listOf("first detector", "failed detector")
    }

    "notificationProgramShouldStopAfterHandlerFailureWithoutMarkingTicketsProcessed" {
        val failure = IllegalStateException()
        val events = mutableListOf<String>()
        val now = LocalDateTime.of(2026, 10, 30, 12, 0)
        val ticket = TicketOpen("show", Platform.INTERPARK, now)
        val settings = NotificationSettings(emptySet(), emptySet(), Path.of("viewed"))
        val ports =
            NotificationPorts(
                read = { IO.pure(emptySet()) },
                detectors = listOf({ IO.pure(listOf(ticket)) }),
                handlers =
                    listOf(
                        { IO.delay<Unit> { events.add("first handler") } },
                        {
                            IO.delay {
                                events.add("failed handler")
                                throw failure
                            }
                        },
                        { IO.delay<Unit> { events.add("skipped handler") } },
                    ),
                write = { _, _ -> IO.delay<Unit> { events.add("write") } },
                now = IO.pure(now),
                log = { IO.pure(Unit) },
            )

        val actual = notificationProgram().run(NotificationEnvironment(settings, ports)).attempt().unsafeRunSync()

        actual shouldBe failure.left()
        events shouldBe listOf("first handler", "failed handler")
    }

    "notificationProgramShouldSurfacePersistenceFailureAfterAllHandlersSucceed" {
        val failure = IllegalStateException()
        val events = mutableListOf<String>()
        val now = LocalDateTime.of(2026, 10, 30, 12, 0)
        val ticket = TicketOpen("show", Platform.INTERPARK, now)
        val settings = NotificationSettings(emptySet(), emptySet(), Path.of("viewed"))
        val ports =
            NotificationPorts(
                read = { IO.pure(emptySet()) },
                detectors = listOf({ IO.pure(listOf(ticket)) }),
                handlers =
                    listOf(
                        { IO.delay<Unit> { events.add("first handler") } },
                        { IO.delay<Unit> { events.add("second handler") } },
                    ),
                write = { _, _ ->
                    IO.delay {
                        events.add("write")
                        throw failure
                    }
                },
                now = IO.pure(now),
                log = { IO.pure(Unit) },
            )

        val actual = notificationProgram().run(NotificationEnvironment(settings, ports)).attempt().unsafeRunSync()

        actual shouldBe failure.left()
        events shouldBe listOf("first handler", "second handler", "write")
    }

    "notificationProgramShouldNotMarkNewTicketsWhenNoHandlersAreConfigured" {
        val now = LocalDateTime.of(2026, 10, 30, 12, 0)
        val old = TicketOpen("already", Platform.INTERPARK, now)
        val fresh = TicketOpen("new", Platform.INTERPARK, now)
        val writes = mutableListOf<Set<TicketOpen>>()
        val settings = NotificationSettings(emptySet(), emptySet(), Path.of("viewed"))
        val ports =
            NotificationPorts(
                read = { IO.pure(setOf(old)) },
                detectors = listOf({ IO.pure(listOf(fresh)) }),
                handlers = emptyList(),
                write = { _, tickets -> IO.delay<Unit> { writes.add(tickets) } },
                now = IO.pure(now),
                log = { IO.pure(Unit) },
            )

        notificationProgram().run(NotificationEnvironment(settings, ports)).unsafeRunSync()

        writes shouldBe listOf(setOf(old))
    }

    "notificationProgramShouldSkipHandlersWhenNoDetectorsAreConfigured" {
        val now = LocalDateTime.of(2026, 10, 30, 12, 0)
        val previous = setOf(TicketOpen("already", Platform.INTERPARK, now))
        val writes = mutableListOf<Set<TicketOpen>>()
        val settings = NotificationSettings(emptySet(), emptySet(), Path.of("viewed"))
        val ports =
            NotificationPorts(
                read = { IO.pure(previous) },
                detectors = emptyList(),
                handlers = listOf({ IO.raiseError(IllegalStateException("Handler must not execute")) }),
                write = { _, tickets -> IO.delay<Unit> { writes.add(tickets) } },
                now = IO.pure(now),
                log = { IO.pure(Unit) },
            )

        notificationProgram().run(NotificationEnvironment(settings, ports)).unsafeRunSync()

        writes shouldBe listOf(previous)
    }
    "notificationProgramShouldApplyRulesBeforeBuildingAndHandlingThePlan" {
        val now = LocalDateTime.of(2026, 10, 30, 12, 0)
        val processed = TicketOpen("keep-old", Platform.INTERPARK, now)
        val selected = processed.copy(name = "keep-show")
        val excluded = processed.copy(name = "keep-excluded")
        val unrelated = processed.copy(name = "other")
        val detected = listOf(processed, selected, excluded, unrelated)
        val received = mutableListOf<NotificationPlan>()
        val writes = mutableListOf<Set<TicketOpen>>()
        val settings =
            NotificationSettings(setOf("keep"), setOf("excluded"), Path.of("viewed"))
        val ports =
            NotificationPorts(
                read = { IO.pure(setOf(processed)) },
                detectors = listOf({ IO.pure(detected) }),
                handlers = listOf({ plan -> IO.delay<Unit> { received.add(plan) } }),
                write = { _, tickets -> IO.delay<Unit> { writes.add(tickets) } },
                now = IO.pure(now),
                log = { IO.pure(Unit) },
            )

        notificationProgram().run(NotificationEnvironment(settings, ports)).unsafeRunSync()

        received.single().detected shouldBe detected
        received.single().selected shouldBe listOf(selected)
        writes shouldBe listOf(setOf(processed, selected))
    }
})
