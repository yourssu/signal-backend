package com.yourssu.signal.api.dto.meeting

import com.yourssu.signal.domain.meeting.business.command.MeetingMemberCommand
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.ProfileValidationPolicy
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MeetingMemberRequest(
    @field:Min(ProfileValidationPolicy.MIN_BIRTH_YEAR)
    val birthYear: Int,

    @field:Size(
        min = ProfileValidationPolicy.MIN_DEPARTMENT_LENGTH,
        max = ProfileValidationPolicy.MAX_DEPARTMENT_LENGTH,
    )
    val department: String,

    @field:NotBlank
    val gender: String,
) {
    fun toCommand() = MeetingMemberCommand(
        birthYear = birthYear,
        department = department,
        gender = Gender.of(gender),
    )
}
