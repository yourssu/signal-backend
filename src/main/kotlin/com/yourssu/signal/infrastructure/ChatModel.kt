package com.yourssu.signal.infrastructure

import com.yourssu.signal.infrastructure.dto.NicknameSuggestedResponse

interface ChatModel {
    fun suggestNickname(statements: List<String>): NicknameSuggestedResponse
}
