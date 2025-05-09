package com.yourssu.signal.domain.profile.business

import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import com.yourssu.signal.domain.profile.business.command.AllProfilesFoundCommand
import com.yourssu.signal.domain.profile.business.command.MtProfileFoundCommand
import com.yourssu.signal.domain.profile.business.command.RandomProfileFoundCommand
import com.yourssu.signal.domain.profile.business.command.TicketConsumedCommand
import com.yourssu.signal.domain.profile.business.dto.ProfileContactResponse
import com.yourssu.signal.domain.profile.business.command.ProfileCreatedCommand
import com.yourssu.signal.domain.profile.business.command.ProfileFoundCommand
import com.yourssu.signal.domain.profile.business.dto.MyProfileResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileResponse
import com.yourssu.signal.domain.profile.implement.*
import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.ViewerReader
import com.yourssu.signal.infrastructure.Notification
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
    fun createProfile(command: ProfileCreatedCommand): MyProfileResponse {
        val profile = command.toDomain()
        genderValidator.validateProfile(profile)
        return MyProfileResponse.from(profileWriter.createProfile(profile))
    }

    fun getProfile(command: MtProfileFoundCommand): MyProfileResponse {
        val profile = profileReader.getByUuid(command.toDomain())
        return MyProfileResponse.from(profile)
    }

    fun getRandomProfile(command: RandomProfileFoundCommand): ProfileResponse {
        val viewer = viewerReader.get(command.toUuid())
        if (profileReader.existsByUuid(command.toUuid())) {
            val myProfile = profileReader.getByUuid(command.toUuid())
            val excludeProfileIds = command.toExcludeProfiles(myProfile)
            val profile = profilePriorityManager.pickRandomProfile(excludeProfileIds, viewer.gender)
            return ProfileResponse.from(profile)
        }
        val profile = profilePriorityManager.pickRandomProfile(command.toExcludeProfiles(), viewer.gender)
        return ProfileResponse.from(profile)
    }

    @Transactional
    fun consumeTicket(command: TicketConsumedCommand): ProfileContactResponse {
        val viewer = viewerReader.get(command.toUuid())
        val targetProfile = profileReader.getById(command.profileId)
        val updatedViewer = usedTicketManager.consumeTicket(
            viewer = viewer,
            profile = targetProfile,
            ticket = policy.contactPrice,
        )
        Notification.notifyConsumedTicket(targetProfile.nickname, updatedViewer.ticket - updatedViewer.usedTicket)
        return ProfileContactResponse.from(targetProfile)
    }

    fun getAllProfiles(command: AllProfilesFoundCommand): List<ProfileContactResponse> {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        return profileReader.getAll()
            .map { ProfileContactResponse.from(it) }
    }

    fun countAllProfiles(): ProfilesCountResponse {
        return ProfilesCountResponse.of(profileReader.countAll())
    }

    fun countByGender(gender: String): ProfilesCountResponse {
        val count = profileReader.count(Gender.of(gender))
        return ProfilesCountResponse.of(count)
    }

    fun getProfile(command: ProfileFoundCommand): ProfileContactResponse {
        val viewer = viewerReader.get(command.toUuid())
        val targetProfile = profileReader.getById(command.profileId)
        usedTicketManager.validatePurchasedProfile(viewer, targetProfile)
        return ProfileContactResponse.from(targetProfile)
    }
}
