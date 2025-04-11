package com.yourssu.ssugaeting.domain.viewer.business

import com.yourssu.ssugaeting.domain.common.implement.Uuid

class VerificationCommand(
    val uuid: String,
) {
    fun toDomain(): Uuid {
        return Uuid(uuid)
    }
}
