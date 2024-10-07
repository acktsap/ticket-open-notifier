import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain(21) // use this version when development
    compilerOptions {
        freeCompilerArgs =
            listOf(
                "-Xjsr305=strict", // enable jsr305 null-safety in kotlin
            )
        jvmTarget = JvmTarget.JVM_21 // make class files for this version
        languageVersion = KotlinVersion.KOTLIN_2_0 // code level
        apiVersion = KotlinVersion.KOTLIN_2_0 // runtime level
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}
