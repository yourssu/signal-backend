package com.yourssu.signal.domain.verification.storage.exception

import com.yourssu.signal.handler.NotFoundException

class VerificationCodeNotFoundException : NotFoundException(message = "A verification code not found for uuid") {
}
