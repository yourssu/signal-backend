package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile
import com.yourssu.signal.domain.profile.implement.exception.GenderNotNullException
import com.yourssu.signal.domain.viewer.implement.ViewerRepository
import org.springframework.stereotype.Component

@Component
class GenderValidator(
    private val profileRepository: ProfileRepository,
    private val viewerRepository: ViewerRepository,
) {
    fun validateProfile(profile: Profile) {
        validateGender(profile.uuid, gender = profile.gender)
    }

    fun validateViewer(uuid: Uuid, gender: Gender?) {
        if (gender != null) {
            validateGender(uuid, gender)
            return
        }
        validateUnRegisteredViewer(uuid)
    }

    private fun validateUnRegisteredViewer(uuid: Uuid) {
        if (!viewerRepository.existsByUuid(uuid)) {
            throw GenderNotNullException()
        }
    }

    private fun validateGender(uuid: Uuid, gender: Gender) {
        if (profileRepository.existsByUuid(uuid)) {
            val profile = profileRepository.getByUuid(uuid)
            profile.validateSameGender(gender)
        }
        if (viewerRepository.existsByUuid(uuid)) {
            val viewer = viewerRepository.getByUuid(uuid)
            viewer.validateSameGender(gender)
        }
    }
}
