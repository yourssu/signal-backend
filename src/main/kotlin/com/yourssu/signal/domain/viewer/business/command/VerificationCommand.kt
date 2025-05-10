package com.yourssu.signal.domain.viewer.business.command

import com.yourssu.signal.domain.common.implement.Uuid

class VerificationCommand(
    val uuid: String,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
}
