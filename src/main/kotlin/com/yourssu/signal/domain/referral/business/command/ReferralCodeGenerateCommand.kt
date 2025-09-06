package com.yourssu.signal.domain.referral.business.command

import com.yourssu.signal.domain.common.implement.Uuid

class ReferralCodeGenerateCommand(
    val uuid: String,
    val customCode: String? = null
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }

    fun referralCode(): String {
        return customCode ?: Uuid.randomUUID().value
    }
}
