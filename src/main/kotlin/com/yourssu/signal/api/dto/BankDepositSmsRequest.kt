package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.viewer.business.command.ProcessDepositSmsCommand
import jakarta.validation.constraints.NotBlank

data class BankDepositSmsRequest(
    @field:NotBlank
    val secretKey: String,

    @field:NotBlank
    val message: String,

    @field:NotBlank
    val type: String,
) {
    fun toCommand(): ProcessDepositSmsCommand {
        return ProcessDepositSmsCommand(
            secretKey = secretKey,
            message = message,
            type = type,
        )
    }
}
