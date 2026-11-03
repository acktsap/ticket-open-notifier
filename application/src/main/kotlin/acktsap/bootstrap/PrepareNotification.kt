package acktsap.bootstrap

import acktsap.adapters.incoming.cli.PrepareNotification
import acktsap.application.NotificationEnvironment
import acktsap.application.NotificationPorts
import acktsap.application.NotificationSettings
import acktsap.application.service.notificationUseCase
import acktsap.configuration.LoadSettings
import acktsap.configuration.MissingSettings
import acktsap.fp.Either
import acktsap.fp.IO
import acktsap.fp.left

fun prepareNotification(
    loadSettings: LoadSettings,
    portsFor: (NotificationSettings) -> IO<Either<MissingSettings, NotificationPorts>>,
): PrepareNotification =
    { overrides ->
        IO.defer { loadSettings(overrides) }.flatMap { result ->
            result.fold(
                ifLeft = { missing -> IO.pure(missing.left()) },
                ifRight = { settings ->
                    IO.defer { portsFor(settings) }.map { configured ->
                        configured.map { ports -> notificationUseCase(NotificationEnvironment(settings, ports)) }
                    }
                },
            )
        }
    }
