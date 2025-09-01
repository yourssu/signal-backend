package com.yourssu.signal.domain.blacklist.business

import com.yourssu.signal.domain.blacklist.business.command.BlacklistAddedCommand
import com.yourssu.signal.domain.blacklist.business.command.BlacklistDeletedCommand
import com.yourssu.signal.domain.blacklist.business.dto.BlacklistExistsResponse
import com.yourssu.signal.domain.blacklist.business.dto.BlacklistResponse
import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import com.yourssu.signal.domain.blacklist.implement.BlacklistWriter
import com.yourssu.signal.domain.blacklist.implement.domain.Blacklist
import com.yourssu.signal.domain.blacklist.implement.exception.AdminBlacklistCannotBeRemovedException
import com.yourssu.signal.domain.blacklist.implement.exception.BlacklistAlreadyExistsException
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class BlacklistService(
    private val blacklistWriter: BlacklistWriter,
    private val blacklistReader: BlacklistReader,
    private val adminAccessChecker: AdminAccessChecker,
    private val profileReader: ProfileReader,
) {
    @Transactional
    fun addBlacklist(command: BlacklistAddedCommand): BlacklistResponse {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val profile = profileReader.getById(command.profileId)
        if (blacklistReader.existsByProfileId(profile.id!!)) {
            val blacklist = blacklistReader.getByProfileId(profile.id)
            blacklistWriter.updateToAdminBlacklist(profileId = profile.id)
            return BlacklistResponse.from(blacklist)
        }
        val blacklist = blacklistWriter.save(command.toDomain())
        return BlacklistResponse.from(blacklist)
    }

    fun removeBlacklist(command: BlacklistDeletedCommand) {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val blacklist = blacklistReader.getByProfileId(command.profileId)
        blacklistWriter.deleteByProfileId(blacklist.profileId)
    }

    fun checkMyBlacklistStatus(uuid: String): BlacklistExistsResponse {
        val profile = profileReader.getByUuid(Uuid(uuid))
        val isBlacklisted = blacklistReader.existsByProfileId(profile.id!!)
        return BlacklistExistsResponse(isBlacklisted = isBlacklisted)
    }

    fun addMyBlacklist(uuid: String): BlacklistResponse {
        val profile = profileReader.getByUuid(Uuid(uuid))
        if (blacklistReader.existsByProfileId(profile.id!!)) {
            throw BlacklistAlreadyExistsException()
        }
        val blacklist = blacklistWriter.save(
            Blacklist(
                profileId = profile.id,
                createdByAdmin = false
            )
        )
        return BlacklistResponse.from(blacklist)
    }

    fun removeMyBlacklist(uuid: String) {
        val profile = profileReader.getByUuid(Uuid(uuid))
        if (!blacklistReader.existsByProfileId(profile.id!!)) {
            return
        }
        if (blacklistReader.isAddedByAdmin(profile.id)) {
            throw AdminBlacklistCannotBeRemovedException()
        }
        blacklistWriter.deleteByProfileId(profile.id)
    }
}
