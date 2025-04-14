package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import org.springframework.stereotype.Component

@Component
class ProfileReader(
    private val profileRepository: ProfileRepository,
) {
    fun getByUuid(uuid: Uuid): Profile {
        return profileRepository.getByUuid(uuid)
    }
}
