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
    implementation(libs.selenium)
    implementation(libs.email.api)
    implementation(libs.email.implementation)
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j)
    implementation(libs.commons.csv)

    runtimeOnly(libs.log4j)

    testImplementation(libs.bundles.test.kotlin)
}
