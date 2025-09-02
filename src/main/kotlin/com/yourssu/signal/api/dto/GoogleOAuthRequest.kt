package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.auth.business.command.GoogleOAuthCommand
import jakarta.validation.constraints.NotBlank

data class GoogleOAuthRequest(
    @field:NotBlank(message = "Authorization code is required")
    val code: String,
) {
    fun toCommand(uuid: String): GoogleOAuthCommand {
        return GoogleOAuthCommand(
            code = code,
            uuid = uuid,
        )
    }
}
