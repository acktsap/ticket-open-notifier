package acktsap.application

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import java.nio.file.Path

data class NotificationSettings(
    val includeKeywords: PersistentSet<String>,
    val excludeKeywords: PersistentSet<String>,
    val visitedFilePath: Path,
    val retentionDays: Long = 7,
) {
    constructor(
        includeKeywords: Set<String>,
        excludeKeywords: Set<String>,
        visitedFilePath: Path,
        retentionDays: Long = 7,
    ) : this(includeKeywords.toPersistentSet(), excludeKeywords.toPersistentSet(), visitedFilePath, retentionDays)
}
