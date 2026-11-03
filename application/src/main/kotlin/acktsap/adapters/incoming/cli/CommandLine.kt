package acktsap.adapters.incoming.cli

import acktsap.configuration.SettingsLayer
import acktsap.fp.Either
import java.nio.file.InvalidPathException
import java.nio.file.Path

sealed interface CommandLineResult {
    data class Run(val settings: SettingsLayer) : CommandLineResult

    data object Help : CommandLineResult

    data class Invalid(val message: String) : CommandLineResult
}

private sealed interface ParsingState {
    data class Options(val visitedFilePath: Path?) : ParsingState

    data object AwaitingPath : ParsingState

    data class Finished(val result: CommandLineResult) : ParsingState
}

fun parseCommandLine(arguments: List<String>): CommandLineResult =
    arguments.fold<String, ParsingState>(ParsingState.Options(null), ::consumeArgument).toResult()

private fun consumeArgument(
    state: ParsingState,
    argument: String,
): ParsingState =
    when (state) {
        is ParsingState.Options ->
            when (argument) {
                "-h" -> ParsingState.Finished(CommandLineResult.Help)
                "-p" -> ParsingState.AwaitingPath
                else -> ParsingState.Finished(CommandLineResult.Invalid("Unknown option: $argument"))
            }
        ParsingState.AwaitingPath ->
            Either.catchOrThrow<InvalidPathException, Path> { Path.of(argument) }.fold(
                ifLeft = { ParsingState.Finished(CommandLineResult.Invalid("Invalid visited file path")) },
                ifRight = ParsingState::Options,
            )
        is ParsingState.Finished -> state
    }

private fun ParsingState.toResult(): CommandLineResult =
    when (this) {
        is ParsingState.Options ->
            visitedFilePath
                ?.let { CommandLineResult.Run(SettingsLayer(visitedFilePath = it)) }
                ?: CommandLineResult.Invalid("Visited file path must be provided")
        ParsingState.AwaitingPath -> CommandLineResult.Invalid("No file path provided after -p")
        is ParsingState.Finished -> result
    }

fun commandLineHelp(): String =
    """Usage:
        | -h         Show help
        | -p <PATH>  Specify the visited file path
    """.trimMargin()
