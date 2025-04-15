package com.yourssu.ssugaeting.domain.viewer.business.command

import com.yourssu.ssugaeting.domain.verification.implement.domain.VerificationCode

class TicketIssuedCommand(
    val secretKey: String,
    val verificationCode: Int,
    val ticket: Int,
) {
    fun toVerificationCode(): VerificationCode {
        return VerificationCode(verificationCode)
    }
}
