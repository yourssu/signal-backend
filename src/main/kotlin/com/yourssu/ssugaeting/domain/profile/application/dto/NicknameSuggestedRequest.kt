package com.yourssu.ssugaeting.domain.profile.application.dto

import jakarta.validation.constraints.NotNull

data class NicknameSuggestedRequest(
    @field:NotNull
    val description: List<String>,
) {
}
