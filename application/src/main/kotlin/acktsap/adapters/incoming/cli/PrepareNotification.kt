package acktsap.adapters.incoming.cli

import acktsap.application.port.incoming.NotifyTickets
import acktsap.configuration.MissingSettings
import acktsap.configuration.SettingsLayer
import acktsap.fp.Either
import acktsap.fp.IO

typealias PrepareNotification = (SettingsLayer) -> IO<Either<MissingSettings, NotifyTickets>>
