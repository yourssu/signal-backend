package com.yourssu.ssugaeting.domain.profile.application.dto

import com.yourssu.ssugaeting.domain.profile.business.command.ProfileCreatedCommand
import jakarta.validation.constraints.NotBlank

data class ProfileCreatedRequest(
    @field:NotBlank
    val gender: String,

    @field:NotBlank
    val animal: String,

    @field:NotBlank
    val contact: String,

    @field:NotBlank
    val mbti: String,

    @field:NotBlank
    val nickname: String,

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
        )
    }

}
