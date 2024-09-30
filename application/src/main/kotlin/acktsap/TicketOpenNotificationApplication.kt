package acktsap

import acktsap.cli.CliArgumentParser
import acktsap.config.Configuration
import acktsap.config.EnvironmentConfiguration
import acktsap.config.InMemoryConfiguration
import acktsap.repository.FileViewedTicketOpenRepository
import kotlin.system.exitProcess

private val targetKeywords =
    listOf(
        "노멀",
        "지킬",
        "시카고",
        "알라딘",
        "아이다",
        "고스트",
        "데스노트",
        "스위니",
    )

fun main(args: Array<String>) {
    val configurationByCli =
        try {
            val cliArgumentParser = CliArgumentParser()
            cliArgumentParser.parse(args)
        } catch (e: UnsupportedOperationException) {
            println(e.message)
            exitProcess(0)
        } catch (e: IllegalArgumentException) {
            System.err.println(e.message)
            exitProcess(1)
        }

    val configuration = baseConfiguration() + configurationByCli
    val visitedFilePath = checkNotNull(configuration.visitedFilePath) { "Visited file path must be provided" }
    val viewedTicketOpenRepository = FileViewedTicketOpenRepository(visitedFilePath)
    viewedTicketOpenRepository.use {
        val ticketOpenNotification = TicketOpenNotification.create(configuration, it)
        ticketOpenNotification.run()
    }
}

private fun baseConfiguration(): Configuration {
    val inMemoryConfiguration = InMemoryConfiguration(targetKeywords = targetKeywords)
    val environmentConfiguration = EnvironmentConfiguration()
    return inMemoryConfiguration + environmentConfiguration
}
