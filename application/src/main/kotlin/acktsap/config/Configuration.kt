package acktsap.config

import java.nio.file.Path

interface Configuration {
    val targetKeywords: List<String>?

    val emailSender: String?

    val emailSenderPassword: String?

    val emailRecipients: List<String>?

    val visitedFilePath: Path?

    infix operator fun plus(other: Configuration): Configuration = merge(other)

    fun merge(other: Configuration): Configuration
}
