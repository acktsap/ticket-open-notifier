plugins {
    id("acktsap.kotlin-conventions")
    id("acktsap.fatjar-conventions")
    id("acktsap.application-conventions")
}

application {
    // Define the main class for the application.
    mainClass = "acktsap.TicketOpenNotificationApplicationKt"
}

dependencies {
    testImplementation(libs.bundles.test.kotlin)
}
