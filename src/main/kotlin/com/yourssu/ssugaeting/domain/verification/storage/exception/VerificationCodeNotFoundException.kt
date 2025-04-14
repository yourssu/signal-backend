package com.yourssu.ssugaeting.domain.verification.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class VerificationCodeNotFoundException : NotFoundException(message = "A verification code not found for uuid") {
}
