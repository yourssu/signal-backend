package com.yourssu.signal.domain.viewer.application.dto

import com.yourssu.signal.domain.viewer.business.command.ViewerFoundCommand
import jakarta.validation.constraints.NotBlank

data class ViewFoundRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): ViewerFoundCommand {
        return ViewerFoundCommand(
            uuid = uuid,
        )
    }
}
