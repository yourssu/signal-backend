package com.yourssu.signal.infrastructure.openai

import com.yourssu.signal.domain.profile.implement.ChatModel
import com.yourssu.signal.infrastructure.openai.dto.NicknameSuggestedResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private const val MAXIMUM_NICKNAME_LENGTH = 10
private const val STATEMENT_SEPARATOR = "&"

@Component
@Profile("local", "test")
class LocalChatModel: ChatModel {
    override fun suggestNickname(statements: List<String>): NicknameSuggestedResponse {
        val nickname = statements.joinToString(STATEMENT_SEPARATOR)
        { it.substring(0, MAXIMUM_NICKNAME_LENGTH / statements.size) }
            .substring(0, MAXIMUM_NICKNAME_LENGTH)
        return NicknameSuggestedResponse(nickname = nickname)
    }
}
