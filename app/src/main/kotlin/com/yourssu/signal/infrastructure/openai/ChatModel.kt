package com.yourssu.signal.infrastructure.openai

import com.yourssu.signal.infrastructure.openai.dto.NicknameSuggestedResponse

interface ChatModel {
    fun suggestNickname(statements: List<String>): NicknameSuggestedResponse
}
