package com.yourssu.signal.domain.profile.application.dto

import com.yourssu.signal.domain.profile.business.command.ProfileCreatedCommand
import com.yourssu.signal.domain.profile.support.ContactFormat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.NumberFormat

data class ProfileCreatedRequest(
    @field:NotBlank
    val gender: String,

    @field:Size(min = 1, max = 20)
    val department: String,

    @field:Min(1900)
    val birthYear: Int,

    @field:NotBlank
    val animal: String,

    @field:ContactFormat
    val contact: String,

    @field:NotBlank
    val mbti: String,

    @field:Size(min = 1, max = 15)
    val nickname: String,

    @field:Size(min = 0, max = 3)
    val introSentences: List<String>,
) {
    fun toCommand(uuid: String): ProfileCreatedCommand {
        return ProfileCreatedCommand(
            gender = gender,
            uuid = uuid,
            department = department,
            birthYear = birthYear,
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences,
        )
    }

}
