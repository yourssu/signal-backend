package com.yourssu.signal.domain.viewer.application.dto

import com.yourssu.signal.domain.viewer.business.dto.SMSTicketIssuedCommand
import jakarta.validation.constraints.NotBlank

class SMSTicketIssuedRequest(
    @field:NotBlank
    val secretKey: String,

    @field:NotBlank
    val message: String,
) {
    fun toCommand(): SMSTicketIssuedCommand {
        return SMSTicketIssuedCommand(
            secretKey = secretKey,
            message = message,
        )
    }
}
