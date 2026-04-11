package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.viewer.business.command.AllViewersFoundCommand
import jakarta.validation.constraints.NotBlank

data class ViewersFoundRequest(
    @field:NotBlank
    val secretKey: String,
) {
    fun toCommand(): AllViewersFoundCommand {
        return AllViewersFoundCommand(
            secretKey = secretKey,
        )
    }
}
