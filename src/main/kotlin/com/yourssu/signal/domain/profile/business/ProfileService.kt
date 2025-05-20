package com.yourssu.signal.domain.profile.business

import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import com.yourssu.signal.domain.profile.business.command.*
import com.yourssu.signal.domain.profile.business.dto.MyProfileResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileContactResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileResponse
import com.yourssu.signal.domain.profile.implement.ProfilePriorityManager
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.profile.implement.ProfileWriter
import com.yourssu.signal.domain.profile.implement.UsedTicketManager
import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.ProfileValidator
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.ViewerReader
import com.yourssu.signal.infrastructure.Notification
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val profileWriter: ProfileWriter,
    private val profileReader: ProfileReader,
    private val viewerReader: ViewerReader,
    private val usedTicketManager: UsedTicketManager,
    private val profilePriorityManager: ProfilePriorityManager,
    private val policy: PolicyConfigurationProperties,
    private val adminAccessChecker: AdminAccessChecker,
) {
    fun createProfile(command: ProfileCreatedCommand): MyProfileResponse {
        val profile = command.toDomain()
        val countContact = profileReader.countContact(profile.contact)
        ProfileValidator.checkContactLimit(countContact, policy.contactLimit)
        ProfileValidator.checkContactLimitWarning(countContact, policy.contactLimitWarning)
        return MyProfileResponse.from(profileWriter.createProfile(profile))
    }

    fun getProfile(command: MtProfileFoundCommand): MyProfileResponse {
        val profile = profileReader.getByUuid(command.toDomain())
        return MyProfileResponse.from(profile)
    }

    fun getRandomProfile(command: RandomProfileFoundCommand): ProfileResponse {
        val gender = command.toGender()
        val uuid = command.toUuid()
        if (profileReader.existsByUuid(uuid)) {
            val myProfile = profileReader.getByUuid(uuid)
            val profile = profilePriorityManager.pickRandomProfile(command.toExcludeProfiles(myProfile), gender)
            return ProfileResponse.from(profile)
        }
        val profile = profilePriorityManager.pickRandomProfile(command.toExcludeProfiles(), gender)
        return ProfileResponse.from(profile)
    }

    fun consumeTicket(command: TicketConsumedCommand): ProfileContactResponse {
        val viewer = viewerReader.get(command.toUuid())
        val targetProfile = profileReader.getById(command.profileId)
        val updatedViewer = usedTicketManager.consumeTicket(
            viewer = viewer,
            profile = targetProfile,
            ticket = policy.contactPrice,
        )
        Notification.notifyConsumedTicket(targetProfile.nickname, updatedViewer.usedTicket - viewer.usedTicket)
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
