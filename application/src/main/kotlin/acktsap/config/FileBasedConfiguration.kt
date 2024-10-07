package acktsap.config

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

private val logger = KotlinLogging.logger { }

class FileBasedConfiguration(
    private val includeKeywordsFile: Path? = null,
    private val excludeKeywordsFile: Path? = null,
) : Configuration {
    override val includeKeywords: List<String>? by lazy {
        if (includeKeywordsFile != null) {
            if (includeKeywordsFile.isDirectory()) {
                throw IllegalArgumentException("$includeKeywordsFile is a directory. Plain file should be provided.")
            }

            if (!includeKeywordsFile.exists()) {
                throw IllegalArgumentException("$includeKeywordsFile doesn't exists.")
            }

            logger.info { "Read includeKeywords from $includeKeywordsFile" }
            readFileAsList(includeKeywordsFile)
        } else {
            null
        }
    }

    override val excludeKeywords: List<String>? by lazy {
        if (excludeKeywordsFile != null) {
            if (excludeKeywordsFile.isDirectory()) {
                throw IllegalArgumentException("$excludeKeywordsFile is a directory. Plain file should be provided.")
            }

            if (!excludeKeywordsFile.exists()) {
                throw IllegalArgumentException("$excludeKeywordsFile doesn't exists.")
            }

            logger.info { "Read excludeKeywords from $excludeKeywordsFile" }
            readFileAsList(excludeKeywordsFile)
        } else {
            null
        }
    }

    override val emailSender: String? = null
    override val emailSenderPassword: String? = null
    override val emailRecipients: List<String>? = null
    override val visitedFilePath: Path? = null

    override fun merge(other: Configuration): Configuration {
        val configurations = listOf(this, other)
        return CompositeConfiguration(
            *configurations.toTypedArray(),
        )
    }

    private fun readFileAsList(path: Path) = path.bufferedReader().readLines()
}
