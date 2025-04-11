package com.yourssu.ssugaeting.domain.viewer.business

import jakarta.validation.constraints.NotBlank


class VerificationCommand(
    @field:NotBlank
    val uuid: String,
) {
    fun toDomain(): String {
        return uuid
    }
}
