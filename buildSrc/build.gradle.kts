plugins {
    `kotlin-dsl` // support convension plugins in kotlin
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal() // give accees to gradle community plugins
}

dependencies {
    // see also (compatibility matrix) : https://docs.gradle.org/current/userguide/compatibility.html
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20")
    implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:12.1.1")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.1")
}
