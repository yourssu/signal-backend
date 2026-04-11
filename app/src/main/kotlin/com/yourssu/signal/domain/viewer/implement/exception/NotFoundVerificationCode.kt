package com.yourssu.signal.domain.viewer.implement.exception

import com.yourssu.signal.handler.NotFoundException

class NotFoundVerificationCode(message: String): NotFoundException(message = "$message is not in verification code list") {
}
