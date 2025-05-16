package com.yourssu.signal.domain.viewer.business.command

import com.yourssu.signal.domain.verification.implement.domain.VerificationCode

class NotificationDepositCommand(
    val message: String,
    val verificationCode: Int
) {
    fun toCode(): VerificationCode {
        return VerificationCode(verificationCode)
    }
}
