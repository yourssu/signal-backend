package com.yourssu.ssugaeting.domain.profile.business

import com.yourssu.ssugaeting.domain.profile.business.dto.ProfileCreatedCommand
import com.yourssu.ssugaeting.domain.profile.business.dto.ProfileResponse
import com.yourssu.ssugaeting.domain.profile.implement.ProfileRepository
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val profileRepository: ProfileRepository,
) {
    fun createProfile(command: ProfileCreatedCommand): ProfileResponse {
        return ProfileResponse.from(profileRepository.save(command.toDomain()))
    }
}
