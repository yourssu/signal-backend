package com.yourssu.signal.domain.profile.application.dto

import com.yourssu.signal.domain.profile.business.command.ProfileFoundCommand
import jakarta.validation.constraints.NotBlank

data class ProfileFoundRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): ProfileFoundCommand {
        return ProfileFoundCommand(
            uuid = uuid,
        )
    }
}
