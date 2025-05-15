package com.yourssu.signal.domain.viewer.application.dto

import com.yourssu.signal.domain.viewer.business.command.NotificationDepositCommand
import jakarta.validation.constraints.NotBlank
import org.springframework.format.annotation.NumberFormat

data class NotificationDepositRequest(
    @field:NotBlank
    val message: String,

    @field:NumberFormat
    val verificationCode: Int,
) {
    fun toCommand(
    ): NotificationDepositCommand {
        return NotificationDepositCommand(
            message = message,
            verificationCode = verificationCode,
        )
    }
}
