package com.yourssu.signal.domain.profile.business.command

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.domain.Profile

class RandomProfileFoundCommand(
    val uuid: String,
    val excludeProfiles: List<Long>,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }

    fun toExcludeProfiles(myProfile: Profile? = null): HashSet<Long> {
        if (myProfile == null) {
            return excludeProfiles.toHashSet()
        }
        return (excludeProfiles + myProfile.id!!).toHashSet()
    }
}
