package com.yourssu.ssugaeting.domain.profile.application.dto

import com.yourssu.ssugaeting.domain.profile.business.ProfileFoundCommand
import jakarta.validation.constraints.NotBlank

class ProfileFoundRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): ProfileFoundCommand {
        return ProfileFoundCommand(
            uuid = uuid,
        )
    }
}
