package com.yourssu.ssugaeting.domain.verification.storage.exception

import com.yourssu.ssugaeting.handler.NotFoundException

class VerificationCodeNotFoundException : NotFoundException(message = "A verification code not found for uuid") {
}
