package com.yourssu.signal.domain.viewer.application.dto

import com.yourssu.signal.domain.profile.business.command.RandomProfileFoundCommand
import jakarta.validation.constraints.NotBlank

data class RandomProfileRequest(
    @field:NotBlank
    val uuid: String,

    val excludeProfiles: List<Long> = emptyList(),
) {
    fun toCommand(): RandomProfileFoundCommand {
        return RandomProfileFoundCommand(
            uuid = uuid,
            excludeProfiles = excludeProfiles
        )
    }
}
