package acktsap.application.port.outgoing

import acktsap.domain.NotificationPlan
import acktsap.fp.IO

typealias NotificationHandler = (NotificationPlan) -> IO<Unit>
