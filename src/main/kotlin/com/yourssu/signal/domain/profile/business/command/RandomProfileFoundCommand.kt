package com.yourssu.signal.domain.profile.business.command

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.Profile

class RandomProfileFoundCommand(
    val uuid: String,
    val excludeProfiles: List<Long>,
    val gender: String,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }

    fun toGender(): Gender {
        return Gender.of(gender)
    }

    fun toExcludeProfiles(myProfile: Profile? = null): HashSet<Long> {
        if (myProfile == null) {
            return excludeProfiles.toHashSet()
        }
        return (excludeProfiles + myProfile.id!!).toHashSet()
    }
}
