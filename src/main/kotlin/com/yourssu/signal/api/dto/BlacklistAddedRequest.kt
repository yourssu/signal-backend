package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.blacklist.business.command.BlacklistAddedCommand
import jakarta.validation.constraints.NotBlank
import org.springframework.format.annotation.NumberFormat

data class BlacklistAddedRequest(
    @NumberFormat
    val profileId: Long,

    @NotBlank
    val secretKey: String
) {
    fun toCommand(): BlacklistAddedCommand {
        return BlacklistAddedCommand(
            profileId = profileId,
            secretKey = secretKey,
        )
    }
}
