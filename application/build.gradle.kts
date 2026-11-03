plugins {
    id("acktsap.kotlin-conventions")
    id("acktsap.fatjar-conventions")
    id("acktsap.application-conventions")
}

application {
    mainClass = "acktsap.MainKt"
}

dependencies {
    implementation(libs.selenium)
    implementation(libs.email.api)
    implementation(libs.email.implementation)
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j)
    implementation(libs.immutable)

    runtimeOnly(libs.log4j)

    testImplementation(libs.bundles.test.kotlin)
}
