package com.yourssu.signal.domain.auth.implement

interface OAuthOutputPort {
    fun verifyOAuthAccessToken(accessToken: String): String?
}
