package com.yourssu.signal.domain.blacklist.business.dto

import com.yourssu.signal.domain.blacklist.implement.domain.Blacklist

class BlacklistResponse(
    val id: Long? = null,
    val profileId: Long,
) {
    companion object {
        fun from(blacklist: Blacklist): BlacklistResponse {
            return BlacklistResponse(
                id = blacklist.id,
                profileId = blacklist.profileId,
            )
        }
    }
}
