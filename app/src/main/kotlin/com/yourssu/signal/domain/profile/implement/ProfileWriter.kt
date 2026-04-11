package com.yourssu.signal.domain.profile.implement

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
        for (gender in Gender.entries) {
            profileRepository.updateCacheIdsByGender(gender)
        }
        return savedProfile
    }

    @Transactional
    fun updateProfile(profile: Profile): Profile {
        val savedProfile = profileRepository.save(profile)
        introSentenceRepository.deleteByUuid(savedProfile.uuid)
        introSentenceRepository.saveAll(introSentences = profile.introSentences, uuid = savedProfile.uuid)
        return savedProfile
    }
}
