package com.yourssu.signal.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.yourssu.signal.domain.profile.business.command.ProfileCreatedCommand
import com.yourssu.signal.domain.profile.implement.ProfileValidationPolicy
import com.yourssu.signal.domain.profile.support.ContactFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProfileCreatedRequest(
    @field:NotBlank
    val gender: String,

    @field:Size(min = ProfileValidationPolicy.MIN_DEPARTMENT_LENGTH, max = ProfileValidationPolicy.MAX_DEPARTMENT_LENGTH)
    val department: String,

    @field:Min(ProfileValidationPolicy.MIN_BIRTH_YEAR)
    val birthYear: Int,

    val animal: String,

    @field:ContactFormat
    @field:Schema(pattern = ProfileValidationPolicy.CONTACT_PATTERN)
    val contact: String,

    val mbti: String,

    @field:Size(min = ProfileValidationPolicy.MIN_NICKNAME_LENGTH, max = ProfileValidationPolicy.MAX_NICKNAME_LENGTH)
    val nickname: String,

    @field:Size(min = ProfileValidationPolicy.MIN_INTRO_SENTENCES_SIZE, max = ProfileValidationPolicy.MAX_INTRO_SENTENCES_SIZE)
    @field:Valid
    val introSentences: List<@Size(max = ProfileValidationPolicy.MAX_INTRO_SENTENCE_LENGTH) String>,

    val school: String? = null,

    val egenTeto: String? = null,
) {
    fun toCommand(uuid: String): ProfileCreatedCommand {
        return ProfileCreatedCommand(
            gender = gender,
            uuid = uuid,
            department = department,
            birthYear = birthYear,
            animal = animal.uppercase(),
            contact = contact,
            mbti = mbti.uppercase(),
            nickname = nickname,
            introSentences = introSentences,
            school = school ?: "숭실대",
            egenTeto = egenTeto,
        )
    }

}
