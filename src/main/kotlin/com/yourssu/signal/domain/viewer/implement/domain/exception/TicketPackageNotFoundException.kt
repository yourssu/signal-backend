package com.yourssu.signal.domain.viewer.implement.domain.exception

import com.yourssu.signal.handler.NotFoundException

class TicketPackageNotFoundException: NotFoundException(message = "Ticket package not found") {
}
