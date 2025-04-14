package com.yourssu.ssugaeting.domain.profile.business

import com.yourssu.ssugaeting.domain.profile.business.dto.ProfileContactResponse
import com.yourssu.ssugaeting.domain.profile.business.dto.ProfileCreatedCommand
import com.yourssu.ssugaeting.domain.profile.implement.ProfileReader
import com.yourssu.ssugaeting.domain.profile.implement.ProfileWriter
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val profileWriter: ProfileWriter,
    private val profileReader: ProfileReader,
) {
    fun createProfile(command: ProfileCreatedCommand): ProfileContactResponse {
        val profile = profileWriter.createProfile(command.toDomain())
        return ProfileContactResponse.from(profile)
    }

    fun getProfile(command: ProfileFoundCommand): ProfileContactResponse {
        val profile = profileReader.getByUuid(command.toDomain())
        return ProfileContactResponse.from(profile)
    }
}
