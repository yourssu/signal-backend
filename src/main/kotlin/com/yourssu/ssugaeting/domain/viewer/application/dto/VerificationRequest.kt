package com.yourssu.ssugaeting.domain.viewer.application.dto

import com.yourssu.ssugaeting.domain.viewer.business.VerificationCommand
import jakarta.validation.constraints.NotBlank

class VerificationRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): VerificationCommand {
        return VerificationCommand(
            uuid = uuid,
        )
    }
}
