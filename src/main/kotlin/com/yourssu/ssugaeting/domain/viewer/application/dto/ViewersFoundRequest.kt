package com.yourssu.ssugaeting.domain.viewer.application.dto

import com.yourssu.ssugaeting.domain.viewer.business.AllViewersFoundCommand
import jakarta.validation.constraints.NotBlank

class ViewersFoundRequest(
    @field:NotBlank
    val secretKey: String,
) {
    fun toCommand(): AllViewersFoundCommand {
        return AllViewersFoundCommand(
            secretKey = secretKey,
        )
    }
}
