package acktsap

import acktsap.config.Configuration
import acktsap.detector.InterparkTicketOpenDetector
import acktsap.detector.TicketOpenDetector
import acktsap.filter.CompositeTicketOpenFilter
import acktsap.filter.ExcludeKeywordsFilter
import acktsap.filter.IncludeKeywordsFilter
import acktsap.filter.MarkAlreadyProcessedFilter
import acktsap.filter.TicketOpenFilter
import acktsap.handler.AlreadyProcessedMarkingHandler
import acktsap.handler.CompositeTicketOpenHandler
import acktsap.handler.GmailNotifyHandler
import acktsap.handler.TicketOpenHandler
import acktsap.repository.FileViewedTicketOpenRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class TicketOpenNotification(
    private val ticketOpenDetector: TicketOpenDetector,
    private val ticketOpenFilter: TicketOpenFilter,
    private val ticketOpenHandler: TicketOpenHandler,
) {
    fun run() {
        val ticketOpens = ticketOpenDetector.detect()
        logger.info { "${ticketOpens.size} ticketOpens detected" }
        ticketOpens.forEach {
            logger.debug { "-- $it" }
        }

        val targetTickerOpens = ticketOpenFilter.doFilter(ticketOpens)
        logger.info { "${targetTickerOpens.size} ticketOpens remains after filtering" }
        targetTickerOpens.forEach {
            logger.debug { "-- $it" }
        }

        ticketOpenHandler.handle(targetTickerOpens)
    }

    companion object {
        fun create(
            configuration: Configuration,
            viewedTicketOpenRepository: FileViewedTicketOpenRepository,
        ): TicketOpenNotification {
            val ticketOpenDetector = InterparkTicketOpenDetector()

            val includeKeywords = configuration.includeKeywords ?: listOf()
            val excludeKeywords = configuration.excludeKeywords ?: listOf()
            val ticketOpenFilter =
                CompositeTicketOpenFilter(
                    MarkAlreadyProcessedFilter(viewedTicketOpenRepository),
                    IncludeKeywordsFilter(*includeKeywords.toTypedArray()),
                    ExcludeKeywordsFilter(*excludeKeywords.toTypedArray()),
                )

            val username = checkNotNull(configuration.emailSender) { "Email sender must be provied" }
            val password = checkNotNull(configuration.emailSenderPassword) { "Email sender password must be provied" }
            val recipients = checkNotNull(configuration.emailRecipients) { "Email recipients must be provied" }
            val ticketOpenHandler =
                CompositeTicketOpenHandler(
                    AlreadyProcessedMarkingHandler(viewedTicketOpenRepository),
                    GmailNotifyHandler(
                        username = username,
                        password = password,
                        recipients = recipients,
                    ),
                )

            return TicketOpenNotification(
                ticketOpenDetector = ticketOpenDetector,
                ticketOpenFilter = ticketOpenFilter,
                ticketOpenHandler = ticketOpenHandler,
            )
        }
    }
}
