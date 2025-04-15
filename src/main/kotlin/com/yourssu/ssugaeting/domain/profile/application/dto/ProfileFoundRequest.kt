package com.yourssu.ssugaeting.domain.profile.application.dto

import com.yourssu.ssugaeting.domain.profile.business.command.ProfileFoundCommand
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
