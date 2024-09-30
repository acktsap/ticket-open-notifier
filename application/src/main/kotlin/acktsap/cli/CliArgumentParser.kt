package acktsap.cli

import acktsap.config.Configuration
import acktsap.config.InMemoryConfiguration
import java.nio.file.Path

class CliArgumentParser {
    /**
     * @throws IllegalArgumentException if invalid argument is provided
     * @throws UnsupportedOperationException if help is printed
     */
    fun parse(args: Array<String>): Configuration {
        var filePath: String? = null

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "-h" -> {
                    // FIXME: is it the best way to handle show help? UnsupportedOperationException???
                    throw UnsupportedOperationException(helpMessage())
                }

                "-p" -> {
                    if (i + 1 < args.size) {
                        filePath = args[i + 1]
                        i++ // Skip the next argument since it's the file path
                    } else {
                        throw IllegalArgumentException("Error: No file path provided after -p")
                    }
                }
            }
            i++
        }

        if (filePath == null) {
            throw IllegalArgumentException("Visited file path must be provided")
        }

        return InMemoryConfiguration(
            visitedFilePath = Path.of(filePath),
        )
    }

    private fun helpMessage(): String {
        val helpMessage = """
            Usage:
            -h         Show help
            -p ${'$'}PATH  Specify the visited file path
            """
        return helpMessage
    }
}
