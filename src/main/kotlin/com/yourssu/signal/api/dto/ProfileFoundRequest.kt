package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.profile.business.command.ProfileFoundCommand
import jakarta.validation.constraints.NotBlank

data class ProfileFoundRequest(
    @NotBlank
    val uuid: String
) {
    fun toCommand(profileId: Long): ProfileFoundCommand {
        return ProfileFoundCommand(
            profileId = profileId,
            uuid = uuid,
        )
    }
}
