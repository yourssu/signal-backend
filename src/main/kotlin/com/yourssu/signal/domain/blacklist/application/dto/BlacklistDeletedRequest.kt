package com.yourssu.signal.domain.blacklist.application.dto

import com.yourssu.signal.domain.blacklist.business.command.BlacklistDeletedCommand
import jakarta.validation.constraints.NotBlank

class BlacklistDeletedRequest(
    @NotBlank
    val secretKey: String
) {
    fun toCommand(profileId: Long): BlacklistDeletedCommand {
        return BlacklistDeletedCommand(
            profileId = profileId,
            secretKey = secretKey,
        )
    }
}
