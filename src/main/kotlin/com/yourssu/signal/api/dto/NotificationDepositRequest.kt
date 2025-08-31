package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.viewer.business.command.NotificationDepositCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.NumberFormat

data class NotificationDepositRequest(
    @field:NotBlank
    @field:Size(max = 20)
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
