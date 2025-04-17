package com.yourssu.signal.domain.profile.business.command

import com.yourssu.signal.domain.common.implement.Uuid

class ProfileFoundCommand(
    val uuid: String
) {
    fun toDomain(): Uuid {
        return Uuid(uuid)
    }
}
