package acktsap.application.port.incoming

import acktsap.fp.IO

typealias NotifyTickets = () -> IO<Unit>
