package com.yourssu.signal.domain.viewer.application.dto

import com.yourssu.signal.domain.profile.business.command.RandomProfileFoundCommand
import com.yourssu.signal.domain.profile.implement.domain.Gender
import jakarta.validation.constraints.NotBlank

data class RandomProfileRequest(
    @field:NotBlank
    val uuid: String,

    @field:NotBlank
    val gender: String,

    val excludeProfiles: List<Long> = emptyList(),
) {
    fun toCommand(): RandomProfileFoundCommand {
        return RandomProfileFoundCommand(
            uuid = uuid,
            gender = gender,
            excludeProfiles = excludeProfiles
        )
    }
}
