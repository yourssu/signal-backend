package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile
import com.yourssu.signal.domain.profile.implement.exception.RandomProfileNotFoundException
import org.springframework.stereotype.Component

@Component
class ProfilePriorityManager(
    private val profileReader: ProfileReader,
) {
    fun pickRandomProfile(
        excludeProfileIds: HashSet<Long>,
        myGender: Gender,
    ): Profile {
        val profileId = profileReader.findAllOppositeGenderIds(myGender)
            .shuffled()
            .firstOrNull { it !in excludeProfileIds }
            ?: throw RandomProfileNotFoundException()
        return profileReader.getById(profileId)
    }
}
