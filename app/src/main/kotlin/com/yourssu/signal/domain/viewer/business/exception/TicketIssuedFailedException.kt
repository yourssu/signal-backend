package com.yourssu.signal.domain.viewer.business.exception

import com.yourssu.signal.handler.BadRequestException

class TicketIssuedFailedException(cause: String = ""): BadRequestException(message = "티켓 발급에 실패했습니다. 원인: $cause") {
}
