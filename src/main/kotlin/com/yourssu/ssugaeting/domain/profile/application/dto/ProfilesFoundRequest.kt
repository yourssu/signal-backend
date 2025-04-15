package com.yourssu.ssugaeting.domain.profile.application.dto

import com.yourssu.ssugaeting.domain.profile.business.AllProfilesFoundCommand
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
