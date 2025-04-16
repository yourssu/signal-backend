package com.yourssu.ssugaeting.domain.profile.business.command

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.domain.Profile

class RandomProfileFoundCommand(
    val uuid: String,
    val excludeProfiles: List<Long>,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }

    fun toExcludeProfiles(myProfile: Profile): HashSet<Long> {
        return (excludeProfiles + myProfile.id!!).toHashSet()
    }
}
