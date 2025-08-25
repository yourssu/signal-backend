package com.yourssu.signal.api.dto

import jakarta.validation.constraints.NotNull

data class NicknameSuggestedRequest(
    @field:NotNull
    val introSentences: List<String>,
) {
}
