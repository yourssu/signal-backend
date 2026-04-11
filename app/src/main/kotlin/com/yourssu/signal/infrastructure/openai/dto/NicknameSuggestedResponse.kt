package com.yourssu.signal.infrastructure.openai.dto

data class NicknameSuggestedResponse(
    val nickname: String,
) {
    companion object {
        fun from(nickname: String): NicknameSuggestedResponse {
            return NicknameSuggestedResponse(nickname = nickname)
        }
    }
}
