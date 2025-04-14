package com.yourssu.ssugaeting.domain.viewer.application.dto

import com.yourssu.ssugaeting.domain.viewer.business.ViewerFoundCommand
import jakarta.validation.constraints.NotBlank

class ViewFoundRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): ViewerFoundCommand {
        return ViewerFoundCommand(
            uuid = uuid,
        )
    }
}
