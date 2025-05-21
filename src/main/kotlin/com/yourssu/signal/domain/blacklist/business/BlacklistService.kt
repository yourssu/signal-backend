package com.yourssu.signal.domain.blacklist.business

import com.yourssu.signal.domain.blacklist.business.command.BlacklistAddedCommand
import com.yourssu.signal.domain.blacklist.business.command.BlacklistDeletedCommand
import com.yourssu.signal.domain.blacklist.business.dto.BlacklistResponse
import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import com.yourssu.signal.domain.blacklist.implement.BlacklistWriter
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import org.springframework.stereotype.Service

@Service
class BlacklistService(
    private val blacklistWriter: BlacklistWriter,
    private val blacklistReader: BlacklistReader,
    private val adminAccessChecker: AdminAccessChecker,
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
}
