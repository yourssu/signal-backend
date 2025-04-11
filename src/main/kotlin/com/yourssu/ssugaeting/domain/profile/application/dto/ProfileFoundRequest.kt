package com.yourssu.ssugaeting.domain.profile.application.dto

import jakarta.validation.constraints.NotBlank

class ProfileFoundRequest(
    @field:NotBlank
    val uuid: String,
) {
    fun toCommand(): Any {
        TODO("Not yet implemented")
    }
}
