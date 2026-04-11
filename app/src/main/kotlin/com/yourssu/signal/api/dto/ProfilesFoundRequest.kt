package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.profile.business.command.AllProfilesFoundCommand
import jakarta.validation.constraints.NotBlank

data class ProfilesFoundRequest(
    @field:NotBlank
    val secretKey: String,
) {
    fun toCommand(): AllProfilesFoundCommand {
        return AllProfilesFoundCommand(
            secretKey = secretKey,
        )
    }
}
