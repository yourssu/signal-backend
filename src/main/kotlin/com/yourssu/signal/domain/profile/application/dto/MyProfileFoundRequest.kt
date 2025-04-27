package com.yourssu.signal.domain.profile.application.dto

import com.yourssu.signal.domain.profile.business.command.MtProfileFoundCommand
import jakarta.validation.constraints.NotBlank

data class MyProfileFoundRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): MtProfileFoundCommand {
        return MtProfileFoundCommand(
            uuid = uuid,
        )
    }
}
