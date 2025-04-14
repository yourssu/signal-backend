package com.yourssu.ssugaeting.domain.profile.business

import com.yourssu.ssugaeting.domain.common.implement.Uuid

class ProfileFoundCommand(
    val uuid: String
) {
    fun toDomain(): Uuid {
        return Uuid(uuid)
    }
}
