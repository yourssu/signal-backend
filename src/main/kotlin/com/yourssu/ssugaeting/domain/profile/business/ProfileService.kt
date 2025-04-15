package com.yourssu.ssugaeting.domain.profile.business

import com.yourssu.ssugaeting.config.PolicyConfigurationProperties
import com.yourssu.ssugaeting.domain.profile.business.dto.ProfileContactResponse
import com.yourssu.ssugaeting.domain.profile.business.dto.ProfileCreatedCommand
import com.yourssu.ssugaeting.domain.profile.business.dto.ProfileResponse
import com.yourssu.ssugaeting.domain.profile.implement.ProfilePriorityManager
import com.yourssu.ssugaeting.domain.profile.implement.ProfileReader
import com.yourssu.ssugaeting.domain.profile.implement.ProfileWriter
import com.yourssu.ssugaeting.domain.viewer.implement.ViewerReader
import com.yourssu.ssugaeting.domain.viewer.implement.ViewerWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileService(
    private val profileWriter: ProfileWriter,
    private val profileReader: ProfileReader,
    private val viewerReader: ViewerReader,
    private val viewerWriter: ViewerWriter,
    private val profilePriorityManager: ProfilePriorityManager,
    private val policy: PolicyConfigurationProperties,
) {
    fun createProfile(command: ProfileCreatedCommand): ProfileContactResponse {
        val profile = profileWriter.createProfile(command.toDomain())
        return ProfileContactResponse.from(profile)
    }

    fun getProfile(command: ProfileFoundCommand): ProfileContactResponse {
        val profile = profileReader.getByUuid(command.toDomain())
        return ProfileContactResponse.from(profile)
    }

    fun getRandomProfile(command: RandomProfileFoundCommand): ProfileResponse {
        val myProfile = profileReader.getByUuid(command.toUuid())
        val excludeProfileIds = command.toExcludeProfiles(myProfile)
        val profile = profilePriorityManager.pickRandomProfile(excludeProfileIds)
        return ProfileResponse.from(profile)
    }

    @Transactional
    fun consumeTicket(command: TicketConsumedCommand): ProfileContactResponse {
        val viewer = viewerReader.get(command.toUuid())
        val targetProfile = profileReader.getById(command.profileId)
        viewerWriter.consumeTicket(viewer, policy.contactPrice)
        return ProfileContactResponse.from(targetProfile)
    }
}
