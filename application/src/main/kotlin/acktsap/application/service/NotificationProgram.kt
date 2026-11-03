package acktsap.application.service

import acktsap.application.NotificationEnvironment
import acktsap.domain.NotificationPlan
import acktsap.domain.TicketOpen
import acktsap.domain.TicketOpenRules
import acktsap.domain.retainRecentTickets
import acktsap.domain.selectTicketOpens
import acktsap.fp.IO
import acktsap.fp.ReaderIO
import acktsap.fp.traverseIO

/**
 * Runs detectors and handlers in registration order. New tickets are recorded only after all configured handlers succeed.
 * With no handlers, new tickets remain unprocessed. Retrying a partial failure may repeat earlier handler effects.
 */
fun notificationProgram(): ReaderIO<NotificationEnvironment, Unit> =
    readPrevious().flatMap { previous ->
        detectTicketOpens().flatMap { detected ->
            preparePlan(detected, previous).flatMap { plan ->
                handle(plan).flatMap { handled -> persist(previous, handled) }
            }
        }
    }

private fun readPrevious(): ReaderIO<NotificationEnvironment, Set<TicketOpen>> =
    ReaderIO { environment ->
        environment.ports.read(environment.settings.visitedFilePath)
            .tap { previous -> environment.ports.log("Loaded ${previous.size} processed ticket opens") }
    }

private fun detectTicketOpens(): ReaderIO<NotificationEnvironment, List<TicketOpen>> =
    ReaderIO { (_, ports) ->
        ports.detectors.traverseIO { detect -> detect() }.map { batches -> batches.flatten().distinct() }
    }

private fun preparePlan(
    detected: List<TicketOpen>,
    previous: Set<TicketOpen>,
): ReaderIO<NotificationEnvironment, NotificationPlan> =
    ReaderIO { (settings, ports) ->
        val rules = TicketOpenRules(settings.includeKeywords, settings.excludeKeywords)
        val plan = NotificationPlan(detected, selectTicketOpens(detected, previous, rules))
        ports.log("Detected ${plan.detected.size}; selected ${plan.selected.size} ticket opens").map { plan }
    }

private fun handle(plan: NotificationPlan): ReaderIO<NotificationEnvironment, Set<TicketOpen>> =
    ReaderIO { (_, ports) ->
        if (plan.selected.isEmpty() || ports.handlers.isEmpty()) {
            IO.pure(emptySet())
        } else {
            ports.handlers.traverseIO { handler -> handler(plan) }.map { plan.selected.toSet() }
        }
    }

private fun persist(
    previous: Set<TicketOpen>,
    handled: Set<TicketOpen>,
): ReaderIO<NotificationEnvironment, Unit> =
    ReaderIO { (settings, ports) ->
        ports.now.flatMap { now ->
            val retained = retainRecentTickets(previous + handled, now.minusDays(settings.retentionDays))
            ports.write(settings.visitedFilePath, retained)
        }
    }
