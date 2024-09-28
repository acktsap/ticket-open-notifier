package acktsap.config

interface Configuration {
    val targetKeywords: List<String>?

    val emailSender: String?

    val emailSenderPassword: String?

    val emailRecipients: List<String>?
}
