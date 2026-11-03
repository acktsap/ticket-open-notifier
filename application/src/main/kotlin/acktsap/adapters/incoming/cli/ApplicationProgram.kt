package acktsap.adapters.incoming.cli

import acktsap.configuration.MissingSettings
import acktsap.fp.Either
import acktsap.fp.IO
import acktsap.fp.left
import acktsap.fp.right

sealed interface ApplicationError {
    data class InvalidCommandLine(val message: String) : ApplicationError

    data class InvalidSettings(val missing: MissingSettings) : ApplicationError
}

fun runApplication(
    program: IO<Either<ApplicationError, Unit>>,
    console: Console,
): IO<Int> =
    program.attempt().flatMap { outcome ->
        outcome.fold(
            ifLeft = { error -> console.printError(error.message ?: error::class.simpleName.orEmpty()).map { 1 } },
            ifRight = { result ->
                result.fold(
                    ifLeft = { error -> console.printError(error.describe()).map { 1 } },
                    ifRight = { IO.pure(0) },
                )
            },
        )
    }

private fun ApplicationError.describe(): String =
    when (this) {
        is ApplicationError.InvalidCommandLine -> message
        is ApplicationError.InvalidSettings -> "Missing required settings: ${missing.fields.joinToString()}"
    }

fun applicationProgram(
    arguments: List<String>,
    console: Console,
    prepareNotification: PrepareNotification,
): IO<Either<ApplicationError, Unit>> {
    val snapshot = arguments.toList()
    return IO.defer {
        when (val commandLine = parseCommandLine(snapshot)) {
            CommandLineResult.Help -> console.printLine(commandLineHelp()).map { it.right() }
            is CommandLineResult.Invalid -> IO.pure(ApplicationError.InvalidCommandLine(commandLine.message).left())
            is CommandLineResult.Run ->
                prepareNotification(commandLine.settings).flatMap { prepared ->
                    prepared.fold(
                        ifLeft = { missing -> IO.pure(ApplicationError.InvalidSettings(missing).left()) },
                        ifRight = { notifyTickets -> IO.defer(notifyTickets).map { it.right() } },
                    )
                }
        }
    }
}
