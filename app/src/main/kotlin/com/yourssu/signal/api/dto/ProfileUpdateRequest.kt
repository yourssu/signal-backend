package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.profile.business.command.ProfileUpdateCommand
import com.yourssu.signal.domain.profile.support.ContactFormat
import jakarta.validation.constraints.Size
import jakarta.validation.Valid

data class ProfileUpdateRequest(
    @field:Size(min = 1, max = 15)
    val nickname: String,

    @field:Size(min = 0, max = 3)
    @field:Valid
    val introSentences: List<@Size(max = 20) String>,

    @field:ContactFormat
    val contact: String,
) {
    fun toCommand(uuid: String): ProfileUpdateCommand {
        return ProfileUpdateCommand(
            uuid = uuid,
            nickname = nickname,
            introSentences = introSentences,
            contact = contact,
        )
    }
}
