package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.referral.business.command.ReferralCodeGenerateCommand

data class ReferralCodeRequest(
    val customCode: String? = null
) {
    fun toCommand(uuid: String): ReferralCodeGenerateCommand {
        return ReferralCodeGenerateCommand(
            uuid = uuid,
            customCode = customCode
        )
    }
}