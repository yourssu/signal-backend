package com.yourssu.ssugaeting.domain.profile.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProfileWriter(
    private val profileRepository: ProfileRepository,
) {
    @Transactional
    fun createProfile(profile: Profile): Profile {
        return profileRepository.save(profile)
    }
}
