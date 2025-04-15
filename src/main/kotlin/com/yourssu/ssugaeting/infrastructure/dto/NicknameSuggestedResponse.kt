package com.yourssu.ssugaeting.infrastructure.dto

data class NicknameSuggestedResponse(
    val nickname: String,
) {
    companion object {
        fun from(nickname: String): NicknameSuggestedResponse {
            return NicknameSuggestedResponse(nickname = nickname)
        }
    }
}
