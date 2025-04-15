package com.yourssu.ssugaeting.domain.profile.application.dto

import jakarta.validation.constraints.NotBlank

data class NicknameGeneratedRequest(
    @field:NotBlank
    val description: List<String>,
) {
}
