package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.domain.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProfileWriter(
    private val profileRepository: ProfileRepository,
    private val introSentenceRepository: IntroSentenceRepository,
) {
    @Transactional
    fun createProfile(profile: Profile): Profile {
        val savedProfile = profileRepository.save(profile)
        introSentenceRepository.saveAll(introSentences = profile.introSentences, uuid = savedProfile.uuid)
        profileRepository.updateCacheProfiles()
        return savedProfile
    }
}
