package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.profile.business.command.ProfileUpdateCommand
import com.yourssu.signal.domain.profile.implement.ProfileValidationPolicy
import com.yourssu.signal.domain.profile.support.ContactFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import jakarta.validation.Valid

data class ProfileUpdateRequest(
    @field:Size(min = ProfileValidationPolicy.MIN_NICKNAME_LENGTH, max = ProfileValidationPolicy.MAX_NICKNAME_LENGTH)
    val nickname: String,

    @field:Size(min = ProfileValidationPolicy.MIN_INTRO_SENTENCES_SIZE, max = ProfileValidationPolicy.MAX_INTRO_SENTENCES_SIZE)
    @field:Valid
    val introSentences: List<@Size(max = ProfileValidationPolicy.MAX_INTRO_SENTENCE_LENGTH) String>,

    @field:ContactFormat
    @field:Schema(pattern = ProfileValidationPolicy.CONTACT_PATTERN)
    val contact: String,

    val egenTeto: String? = null,
) {
    fun toCommand(uuid: String): ProfileUpdateCommand {
        return ProfileUpdateCommand(
            uuid = uuid,
            nickname = nickname,
            introSentences = introSentences,
            contact = contact,
            egenTeto = egenTeto,
        )
    }
}
