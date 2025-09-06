package com.yourssu.signal.domain.payment.implement.exception

import com.yourssu.signal.handler.BadRequestException

class InvalidOrderStatusException : BadRequestException(message = "Invalid order status for this operation.") {
}
