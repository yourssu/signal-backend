package com.yourssu.ssugaeting.infrastructure

import com.yourssu.ssugaeting.infrastructure.dto.NicknameSuggestedResponse

interface ChatModel {
    fun suggestNickname(statements: List<String>): NicknameSuggestedResponse
}
