package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.profile.implement.exception.RandomProfileNotFoundException
import org.springframework.stereotype.Component

@Component
class ProfilePriorityManager(
    private val profileRepository: ProfileRepository,
) {
    fun pickRandomProfile(
        excludeProfileIds: Set<Long>,
    ): Profile {
        return profileRepository.findAll()
            .shuffled()
            .firstOrNull { profile -> profile.id !in excludeProfileIds }
            ?: throw RandomProfileNotFoundException()
    }
}
