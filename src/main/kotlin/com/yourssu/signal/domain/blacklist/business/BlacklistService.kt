package com.yourssu.signal.domain.blacklist.business

import com.yourssu.signal.domain.blacklist.business.command.BlacklistAddedCommand
import com.yourssu.signal.domain.blacklist.business.command.BlacklistDeletedCommand
import com.yourssu.signal.domain.blacklist.business.dto.BlacklistExistsResponse
import com.yourssu.signal.domain.blacklist.business.dto.BlacklistResponse
import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import com.yourssu.signal.domain.blacklist.implement.BlacklistWriter
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import org.springframework.stereotype.Service

@Service
class BlacklistService(
    private val blacklistWriter: BlacklistWriter,
    private val blacklistReader: BlacklistReader,
    private val adminAccessChecker: AdminAccessChecker,
    private val profileReader: ProfileReader,
) {
    fun addBlacklist(command: BlacklistAddedCommand): BlacklistResponse {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        if (blacklistReader.existsByProfileId(command.profileId)) {
            val blacklist = blacklistReader.getByProfileId(command.profileId)
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
}
