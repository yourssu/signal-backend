package com.yourssu.ssugaeting.domain.profile.implement

import org.springframework.stereotype.Component

@Component
class ProfileWriter(
    private val profileRepository: ProfileRepository,
) {
    fun createProfile(profile: Profile): Profile {
        val savedProfile = profileRepository.save(profile)
        profileRepository.updateCacheProfiles()
        return savedProfile
    }
}
