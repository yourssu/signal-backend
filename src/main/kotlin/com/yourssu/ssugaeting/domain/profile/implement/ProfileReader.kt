package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.domain.Gender
import com.yourssu.ssugaeting.domain.profile.implement.domain.Profile
import org.springframework.stereotype.Component

@Component
class ProfileReader(
    private val profileRepository: ProfileRepository,
    private val introSentenceRepository: IntroSentenceRepository,
) {
    fun getByUuid(uuid: Uuid): Profile {
        val profile = profileRepository.getByUuid(uuid)
        val introSentences = introSentenceRepository.findAllByUuid(uuid)
        return profile.copy(introSentences = introSentences)
    }

    fun getById(id: Long): Profile {
        val profile = profileRepository.getById(id)
        val introSentences = introSentenceRepository.findAllByUuid(profile.uuid)
        return profile.copy(introSentences = introSentences)
    }

    fun getAll(): List<Profile> {
        return profileRepository.findAll()
    }

    fun findAllOppositeGenderIds(myGender: Gender): List<Long> {
        return profileRepository.findAllOppositeGenderIds(myGender)
    }
}
