package com.yourssu.signal.domain.viewer.application.dto

import com.yourssu.signal.domain.viewer.business.command.VerificationCommand
import jakarta.validation.constraints.NotBlank

data class VerificationRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): VerificationCommand {
        return VerificationCommand(
            uuid = uuid,
        )
    }
}
