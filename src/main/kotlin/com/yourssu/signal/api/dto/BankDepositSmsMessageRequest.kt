package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.viewer.business.command.ProcessDepositSmsCommand
import jakarta.validation.constraints.NotBlank

data class BankDepositSmsMessageRequest(
    @field:NotBlank
    val message: String,
) {
    fun toCommand(secretKey: String, type: String): ProcessDepositSmsCommand {
        return ProcessDepositSmsCommand(
            secretKey = secretKey,
            message = message,
            type = type,
        )
    }
}