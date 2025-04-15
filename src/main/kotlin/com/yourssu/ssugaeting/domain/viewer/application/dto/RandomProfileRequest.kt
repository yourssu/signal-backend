package com.yourssu.ssugaeting.domain.viewer.application.dto

import com.yourssu.ssugaeting.domain.profile.business.command.RandomProfileFoundCommand
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
