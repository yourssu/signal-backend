package com.yourssu.signal.domain.profile.application.dto

import jakarta.validation.constraints.NotNull

data class NicknameSuggestedRequest(
    @field:NotNull
    val introSentences: List<String>,
) {
}
