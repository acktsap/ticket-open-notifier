package acktsap.application.service

import acktsap.application.NotificationEnvironment
import acktsap.application.port.incoming.NotifyTickets

fun notificationUseCase(environment: NotificationEnvironment): NotifyTickets = { notificationProgram().run(environment) }
