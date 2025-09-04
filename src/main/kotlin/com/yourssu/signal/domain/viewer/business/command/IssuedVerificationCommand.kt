package com.yourssu.signal.domain.viewer.business.command

import com.yourssu.signal.domain.common.implement.Uuid

class IssuedVerificationCommand(
    val uuid: String,
    val referralCode: String?,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
}
