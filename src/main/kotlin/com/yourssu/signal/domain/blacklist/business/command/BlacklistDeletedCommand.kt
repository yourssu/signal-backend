package com.yourssu.signal.domain.blacklist.business.command

class BlacklistDeletedCommand(
    val profileId: Long,
    val secretKey: String,
) {
    fun toDomain(): BlacklistDeletedCommand {
        return BlacklistDeletedCommand(
            profileId = profileId,
            secretKey = secretKey,
        )
    }
}
