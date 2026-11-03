package acktsap.adapters.outgoing.configuration

import acktsap.application.NotificationSettings
import acktsap.configuration.LoadSettings
import acktsap.configuration.SettingsLayer
import acktsap.configuration.resolveSettings
import acktsap.fp.IO
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

private const val INCLUDE_KEYWORD_FILE = "include_keywords.txt"
private const val EXCLUDE_KEYWORD_FILE = "exclude_keywords.txt"

fun systemSettings(): LoadSettings =
    { commandLineSettings ->
        IO.delay {
            val environmentSettings = settingsLayerFromEnvironment(System.getenv())
            val applicationDirectory = Path.of(NotificationSettings::class.java.protectionDomain.codeSource.location.toURI()).parent
            val includePath = applicationDirectory.resolve(INCLUDE_KEYWORD_FILE)
            val excludePath = applicationDirectory.resolve(EXCLUDE_KEYWORD_FILE)
            val fileSettings =
                settingsLayerFromKeywordFiles(
                    includeKeywords = includePath.takeIf(Path::exists)?.readLines().orEmpty(),
                    excludeKeywords = excludePath.takeIf(Path::exists)?.readLines().orEmpty(),
                )
            listOf(commandLineSettings, environmentSettings, fileSettings)
        }.map(::resolveSettings)
    }

fun settingsLayerFromEnvironment(environment: Map<String, String>): SettingsLayer =
    SettingsLayer(
        includeKeywords = environment["INCLUDE_KEYWORDS"]?.splitValues(),
        excludeKeywords = environment["EXCLUDE_KEYWORDS"]?.splitValues(),
        visitedFilePath = environment["VISITED_FILE_PATH"]?.let(Path::of),
    )

fun settingsLayerFromKeywordFiles(
    includeKeywords: List<String>,
    excludeKeywords: List<String>,
): SettingsLayer =
    SettingsLayer(
        includeKeywords = includeKeywords.map(String::trim).filter(String::isNotEmpty),
        excludeKeywords = excludeKeywords.map(String::trim).filter(String::isNotEmpty),
    )

private fun String.splitValues(): List<String> = split(',').map(String::trim).filter(String::isNotEmpty)
