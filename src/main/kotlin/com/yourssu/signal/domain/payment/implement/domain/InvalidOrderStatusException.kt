package com.yourssu.signal.domain.payment.implement.domain

import com.yourssu.signal.handler.BadRequestException

class InvalidOrderStatusException : BadRequestException(message = "Invalid order status for this operation.") {
}
