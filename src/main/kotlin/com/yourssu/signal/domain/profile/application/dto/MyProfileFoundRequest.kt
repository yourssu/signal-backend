package com.yourssu.signal.domain.profile.application.dto

import com.yourssu.signal.domain.profile.business.command.MyProfileFoundCommand
import jakarta.validation.constraints.NotBlank

data class MyProfileFoundRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): MyProfileFoundCommand {
        return MyProfileFoundCommand(
            uuid = uuid,
        )
    }
}
