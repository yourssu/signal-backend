package com.yourssu.signal.infrastructure.openai.dto

import kotlinx.serialization.Serializable

@Serializable
data class NicknameSuggestedRequest(
    val model: String,
    val messages: List<ContentRequest>,
) {
    companion object {
        fun from(model: String, messages: List<ContentRequest>): NicknameSuggestedRequest {
            return NicknameSuggestedRequest(
                model = model,
                messages = messages,
            )
        }
    }
}

@Serializable
data class ContentRequest(
    val role: String,
    val content: String,
) {
    companion object {
        fun of(role: String, content: String): ContentRequest {
            return ContentRequest(
                role = role,
                content = content,
            )
        }
    }
}
