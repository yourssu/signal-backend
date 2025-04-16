package com.yourssu.ssugaeting.domain.viewer.application.dto

import com.yourssu.ssugaeting.domain.viewer.business.command.VerificationCommand
import jakarta.validation.constraints.NotBlank

data class VerificationRequest(
    @field:NotBlank
    val uuid: String,

    val gender: String? = null,
) {
    fun toCommand(): VerificationCommand {
        return VerificationCommand(
            uuid = uuid,
            gender = gender,
        )
    }
}
