package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.viewer.business.command.IssuedVerificationCommand

data class IssuedVerificationRequest(
    val referralCode: String? = null,
) {
    fun toCommand(uuid: String): IssuedVerificationCommand {
        return IssuedVerificationCommand(
            uuid = uuid,
            referralCode = referralCode
        )
    }
}
