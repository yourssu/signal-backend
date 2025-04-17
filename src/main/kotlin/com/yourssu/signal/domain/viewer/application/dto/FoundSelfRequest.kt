package com.yourssu.signal.domain.viewer.application.dto

import com.yourssu.signal.domain.viewer.business.command.ViewerFoundCommand
import jakarta.validation.constraints.NotBlank

data class FoundSelfRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): ViewerFoundCommand {
        return ViewerFoundCommand(
            uuid = uuid,
        )
    }
}
