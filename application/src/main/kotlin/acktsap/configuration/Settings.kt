package acktsap.configuration

import acktsap.application.NotificationSettings
import acktsap.fp.Either
import acktsap.fp.Validated
import java.nio.file.Path

data class SettingsLayer(
    val includeKeywords: List<String>? = null,
    val excludeKeywords: List<String>? = null,
    val visitedFilePath: Path? = null,
)

data class MissingSettings(val fields: List<String>)

fun resolveSettings(layersByPriority: List<SettingsLayer>): Either<MissingSettings, NotificationSettings> {
    val includeKeywords = layersByPriority.firstNotNullOfOrNull { it.includeKeywords }.orEmpty().toSet()
    val excludeKeywords = layersByPriority.firstNotNullOfOrNull { it.excludeKeywords }.orEmpty().toSet()
    val visitedFilePath = layersByPriority.firstNotNullOfOrNull { it.visitedFilePath }

    return Validated.required(visitedFilePath, "visited file path").toEither()
        .map { path -> NotificationSettings(includeKeywords, excludeKeywords, path) }
        .mapLeft(::MissingSettings)
}
