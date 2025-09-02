package com.yourssu.signal.domain.auth.implement

import org.springframework.stereotype.Component

@Component
class GoogleUserWriter(
    private val googleUserRepository: GoogleUserRepository,
) {
    fun save(googleUser: GoogleUser): GoogleUser {
        return googleUserRepository.save(googleUser)
    }
}
