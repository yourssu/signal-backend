package com.yourssu.signal.domain.profile.application.dto

import com.yourssu.signal.domain.profile.business.command.ProfileCreatedCommand
import com.yourssu.signal.domain.profile.support.ContactFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProfileCreatedRequest(
    @field:NotBlank
    val gender: String,

    @field:NotBlank
    val animal: String,

    @field:ContactFormat
    val contact: String,

    @field:NotBlank
    val mbti: String,

    @field:Size(min = 1, max = 10)
    val nickname: String,

    @field:Size(min = 0, max = 3)
    val introSentences: List<String>,

    val uuid: String? = null,
) {
    fun toCommand(): ProfileCreatedCommand {
        return ProfileCreatedCommand(
            uuid = uuid,
            gender = gender,
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences,
        )
    }

}
