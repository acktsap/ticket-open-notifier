package acktsap.configuration

import acktsap.application.NotificationSettings
import acktsap.fp.Either
import acktsap.fp.IO

typealias LoadSettings = (SettingsLayer) -> IO<Either<MissingSettings, NotificationSettings>>
