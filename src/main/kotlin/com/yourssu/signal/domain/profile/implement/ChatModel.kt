package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.infrastructure.openai.dto.NicknameSuggestedResponse

interface ChatModel {
    fun suggestNickname(statements: List<String>): NicknameSuggestedResponse
}
