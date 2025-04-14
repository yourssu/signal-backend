package com.yourssu.ssugaeting.domain.profile.business

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.Profile

class RandomProfileFoundCommand(
    val uuid: String,
    val excludeProfiles: List<Long>,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }

    fun toExcludeProfiles(myProfile: Profile): Set<Long> {
        return (excludeProfiles + myProfile.id!!).toSet()
    }
}
