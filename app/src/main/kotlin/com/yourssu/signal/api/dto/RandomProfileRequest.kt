package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.profile.business.command.RandomProfileFoundCommand
import jakarta.validation.constraints.NotBlank

data class RandomProfileRequest(
    @field:NotBlank
    val gender: String,

    val excludeProfiles: List<Long> = emptyList(),
) {
    fun toCommand(uuid: String): RandomProfileFoundCommand {
        return RandomProfileFoundCommand(
            uuid = uuid,
            gender = gender,
            excludeProfiles = excludeProfiles
        )
    }
}
