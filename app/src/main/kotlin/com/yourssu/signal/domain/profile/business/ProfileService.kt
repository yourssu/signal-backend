package com.yourssu.signal.domain.profile.business

import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import com.yourssu.signal.domain.blacklist.implement.Blacklist
import com.yourssu.signal.domain.blacklist.implement.BlacklistWriter
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.business.command.*
import com.yourssu.signal.domain.profile.business.dto.MyProfileResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileContactResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileRankingResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileResponse
import com.yourssu.signal.domain.profile.implement.*
import com.yourssu.signal.domain.user.implement.UserReader
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.ViewerReader
import com.yourssu.signal.infrastructure.logging.Notification
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val profileWriter: ProfileWriter,
    private val profileReader: ProfileReader,
    private val userReader: UserReader,
    private val viewerReader: ViewerReader,
    private val usedTicketManager: UsedTicketManager,
    private val profilePriorityManager: ProfilePriorityManager,
    private val purchasedProfileReader: PurchasedProfileReader,
    private val policy: PolicyConfigurationProperties,
    private val adminAccessChecker: AdminAccessChecker,
    private val blacklistWriter: BlacklistWriter,
) {
    fun createProfile(command: ProfileCreatedCommand): MyProfileResponse {
        userReader.getByUuid(command.toUuid())
        val profile = command.toDomain()
        val countContact = profileReader.countContact(profile.contact)
        ProfileValidator.checkContactLimit(countContact, policy.contactLimit)
        ProfileValidator.checkContactLimitWarning(countContact, policy.contactLimitWarning)
        val createdProfile = profileWriter.createProfile(profile)
        Notification.notifyCreatedProfile(createdProfile.copy(profile.introSentences))
        if (policy.whitelist) {
            blacklistWriter.save(Blacklist(profileId = createdProfile.id!!, createdByAdmin = true))
        }
        return MyProfileResponse.from(createdProfile)
    }

    fun getProfile(uuid: String): MyProfileResponse {
        val profile = profileReader.getByUuid(Uuid(uuid))
        return MyProfileResponse.from(profile)
    }

    fun updateProfile(command: ProfileUpdateCommand): MyProfileResponse {
        val profile = profileReader.getByUuid(Uuid(command.uuid))
        val updatedProfile = profile.copy(
            nickname = command.nickname,
            introSentences = command.introSentences,
            contact = command.contact
        )
        val countContact = profileReader.countContact(command.contact)
        ProfileValidator.checkContactLimit(countContact, policy.contactLimit + 1)
        val savedProfile = profileWriter.updateProfile(updatedProfile)
        return MyProfileResponse.from(savedProfile)
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

    fun getProfileRanking(uuid: String): ProfileRankingResponse {
        val profile = profileReader.getByUuid(Uuid(uuid))
        val ranking = purchasedProfileReader.getProfileRanking(profile.id!!, profile.gender)
        val totalProfiles = profileReader.count(profile.gender)
        return ProfileRankingResponse.of(profileRanking = ranking, profile = profile, totalProfiles = totalProfiles)
    }

    fun getPurchasedProfiles(uuid: String): List<ProfileContactResponse> {
        val viewer = viewerReader.get(Uuid(uuid))
        val profileIds = purchasedProfileReader.findByViewerId(viewer.id!!)
            .map { it.profileId }
        val profiles = profileReader.getByIds(profileIds)
        return profiles.map { it -> ProfileContactResponse.from(it) }
    }
}
