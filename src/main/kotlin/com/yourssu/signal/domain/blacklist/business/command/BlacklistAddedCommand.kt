package com.yourssu.signal.domain.blacklist.business.command

import com.yourssu.signal.domain.blacklist.implement.Blacklist

class BlacklistAddedCommand(
    val profileId: Long,
    val secretKey: String,
) {
    fun toDomain(): Blacklist {
        return Blacklist(
            profileId = profileId,
            createdByAdmin = true,
        )
    }
}
