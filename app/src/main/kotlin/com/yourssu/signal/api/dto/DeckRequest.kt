package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.profile.business.command.DeckCommand
import jakarta.validation.constraints.NotBlank

data class DeckRequest(
    @field:NotBlank
    val gender: String,
) {
    fun toCommand(uuid: String) = DeckCommand(uuid = uuid, gender = gender)
}
