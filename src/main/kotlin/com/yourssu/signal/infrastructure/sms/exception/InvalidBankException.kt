package com.yourssu.signal.infrastructure.sms.exception

import com.yourssu.signal.handler.BadRequestException

class InvalidBankException(type: String): BadRequestException(message = "Unknown Bank type: $type") {
}
