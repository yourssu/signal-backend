package com.yourssu.signal.domain.viewer.business.command

import VerificationCode

class TicketIssuedCommand(
    val secretKey: String,
    val verificationCode: Int,
    val ticket: Int,
) {
    fun toVerificationCode(): VerificationCode {
        return VerificationCode(verificationCode)
    }
}
