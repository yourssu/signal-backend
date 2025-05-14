package com.yourssu.signal.domain.viewer.application.dto

import com.yourssu.signal.domain.viewer.business.dto.ProcessDepositSmsCommand
import jakarta.validation.constraints.NotBlank

class BankDepositSmsRequest(
    @field:NotBlank
    val secretKey: String,

    @field:NotBlank
    val message: String,
) {
    fun toCommand(): ProcessDepositSmsCommand {
        return ProcessDepositSmsCommand(
            secretKey = secretKey,
            message = message,
        )
    }
}
