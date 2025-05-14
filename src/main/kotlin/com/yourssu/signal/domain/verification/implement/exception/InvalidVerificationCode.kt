package com.yourssu.signal.domain.verification.implement.exception

import com.yourssu.signal.handler.BadRequestException

class InvalidVerificationCode: BadRequestException(message = "Invalid verification code") {
}
