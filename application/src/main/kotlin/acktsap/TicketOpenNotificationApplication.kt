package acktsap

import acktsap.cli.CliArgumentParser
import acktsap.config.Configuration
import acktsap.config.EnvironmentConfiguration
import acktsap.config.InMemoryConfiguration
import acktsap.repository.FileViewedTicketOpenRepository
import kotlin.system.exitProcess

private const val INCLUDE_KEYWORD_FILE = "include_keywords.txt"
private const val TICKETOPEN_KEEP_DATE = 7L

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
    val viewedTicketOpenRepository = FileViewedTicketOpenRepository(visitedFilePath, TICKETOPEN_KEEP_DATE)
    viewedTicketOpenRepository.use {
        val ticketOpenNotification = TicketOpenNotification.create(configuration, it)
        ticketOpenNotification.run()
    }
}

private fun baseConfiguration(): Configuration {
    val targetKeywords = loadKeywords()
    val inMemoryConfiguration = InMemoryConfiguration(includeKeywords = targetKeywords)
    val environmentConfiguration = EnvironmentConfiguration()
    return inMemoryConfiguration + environmentConfiguration
}

private fun loadKeywords(): List<String> {
    val classLoader = Thread.currentThread().contextClassLoader
    val file = INCLUDE_KEYWORD_FILE
    val resourceAsStream =
        checkNotNull(classLoader.getResourceAsStream(file)) { "Can't find keyword files (name: $file)" }
    return resourceAsStream
        .bufferedReader()
        .readLines()
}
