package com.yourssu.ssugaeting.domain.viewer.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

class TicketCreatedRequest(
    @field:NotBlank
    val secretKey: String,

    @field:NotBlank
    val verificationCode: Int,

    @field:Positive
    val ticket: Int,
) {
}
