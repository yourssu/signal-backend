package com.yourssu.signal.domain.auth.implement

interface OAuthOutputPort {
    fun exchangeCodeForIdToken(code: String): String?
}
