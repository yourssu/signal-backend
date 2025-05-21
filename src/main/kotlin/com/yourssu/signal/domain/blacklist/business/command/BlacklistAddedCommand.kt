package com.yourssu.signal.domain.blacklist.business.command

import com.yourssu.signal.domain.blacklist.implement.domain.Blacklist

class BlacklistAddedCommand(
    val profileId: Long,
    val secretKey: String,
) {
    fun toDomain(): Blacklist {
        return Blacklist(
            profileId = profileId,
        )
    }
}
