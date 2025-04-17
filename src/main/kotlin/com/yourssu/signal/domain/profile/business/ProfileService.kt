package com.yourssu.signal.domain.profile.business

import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import com.yourssu.signal.domain.profile.business.command.AllProfilesFoundCommand
import com.yourssu.signal.domain.profile.business.command.ProfileFoundCommand
import com.yourssu.signal.domain.profile.business.command.RandomProfileFoundCommand
import com.yourssu.signal.domain.profile.business.command.TicketConsumedCommand
import com.yourssu.signal.domain.profile.business.dto.ProfileContactResponse
import com.yourssu.signal.domain.profile.business.command.ProfileCreatedCommand
import com.yourssu.signal.domain.profile.business.dto.MyProfileResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileResponse
import com.yourssu.signal.domain.profile.implement.*
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.ViewerReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileService(
    private val profileWriter: ProfileWriter,
    private val profileReader: ProfileReader,
    private val viewerReader: ViewerReader,
    private val genderValidator: GenderValidator,
    private val usedTicketManager: UsedTicketManager,
    private val profilePriorityManager: ProfilePriorityManager,
    private val policy: PolicyConfigurationProperties,
    private val adminAccessChecker: AdminAccessChecker,
) {
    fun createProfile(command: ProfileCreatedCommand): ProfileContactResponse {
        val profile = command.toDomain()
        genderValidator.validateProfile(profile)
        return ProfileContactResponse.from(profileWriter.createProfile(profile))
    }

    fun getProfile(command: ProfileFoundCommand): MyProfileResponse {
        val profile = profileReader.getByUuid(command.toDomain())
        return MyProfileResponse.from(profile)
    }

    fun getRandomProfile(command: RandomProfileFoundCommand): ProfileResponse {
        val myProfile = profileReader.getByUuid(command.toUuid())
        val excludeProfileIds = command.toExcludeProfiles(myProfile)
        val profile = profilePriorityManager.pickRandomProfile(excludeProfileIds, myProfile.gender)
        return ProfileResponse.from(profile)
    }

    @Transactional
    fun consumeTicket(command: TicketConsumedCommand): ProfileContactResponse {
        val viewer = viewerReader.get(command.toUuid())
        val targetProfile = profileReader.getById(command.profileId)
        usedTicketManager.consumeTicket(
            viewer = viewer,
            profile = targetProfile,
            ticket = policy.contactPrice,
        )
        return ProfileContactResponse.from(targetProfile)
    }

    fun getAllProfiles(command: AllProfilesFoundCommand): List<ProfileResponse> {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        return profileReader.getAll()
            .map { ProfileResponse.from(it) }
    }

    fun countAllProfiles(): ProfilesCountResponse {
        return ProfilesCountResponse.of(profileReader.countAll())
    }
}
