package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.BannedWordException

private val SPECIAL_CHARS = Regex("[^가-힣a-z0-9]")
private val REPEATED_CHARS = Regex("(.)\\1+")

object BannedWordValidator {
    fun validate(text: String, bannedWords: Set<String>) {
        val normalized = normalize(text)
        if (bannedWords.any { normalized.contains(it) }) {
            throw BannedWordException()
        }
    }

    private fun normalize(text: String): String {
        val lower = text.lowercase()
        val stripped = lower.replace(SPECIAL_CHARS, "")
        return stripped.replace(REPEATED_CHARS, "$1")
    }
}
