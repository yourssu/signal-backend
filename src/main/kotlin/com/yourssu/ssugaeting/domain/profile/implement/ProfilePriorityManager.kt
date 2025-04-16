package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.profile.implement.domain.Profile
import com.yourssu.ssugaeting.domain.profile.implement.exception.RandomProfileNotFoundException
import org.springframework.stereotype.Component

@Component
class ProfilePriorityManager(
    private val profileReader: ProfileReader,
) {
    fun pickRandomProfile(
        excludeProfileIds: HashSet<Long>,
    ): Profile {
        val profileId = profileReader.findAllIds()
            .shuffled()
            .firstOrNull { it !in excludeProfileIds }
            ?: throw RandomProfileNotFoundException()
        return profileReader.getById(profileId)
    }
}
