package acktsap.config

class InMemoryConfiguration(
    override val targetKeywords: List<String>?,
    override val emailSender: String?,
    override val emailSenderPassword: String?,
    override val emailRecipients: List<String>?,
) : Configuration
