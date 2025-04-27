package com.yourssu.signal.domain.profile.business.command

import com.yourssu.signal.domain.common.implement.Uuid

class ProfileFoundCommand(
    val profileId: Long,
    val uuid: String,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
}
