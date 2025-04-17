package com.yourssu.signal.infrastructure.dto

import kotlinx.serialization.Serializable

@Serializable
data class NicknameSuggestedRequest(
    val model: String,
    val input: List<ContentRequest>,
) {
    companion object {
        fun from(model: String, input: List<ContentRequest>): NicknameSuggestedRequest {
            return NicknameSuggestedRequest(
                model = model,
                input = input,
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
