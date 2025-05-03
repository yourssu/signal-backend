package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile
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

    fun existsByUuid(uuid: Uuid): Boolean {
        return profileRepository.existsByUuid(uuid)
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
        return profileRepository.findIdsByGender(myGender.opposite())
    }

    fun countAll(): Int {
        return Gender.entries
            .stream()
            .mapToInt { profileRepository.findIdsByGender(it).size }
            .sum()
    }

    fun count(gender: Gender): Int {
        return profileRepository.findIdsByGender(gender).size
    }
}
