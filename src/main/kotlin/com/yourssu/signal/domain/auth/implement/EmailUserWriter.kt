package com.yourssu.signal.domain.auth.implement

import org.springframework.stereotype.Component

@Component
class EmailUserWriter(
    private val emailUserRepository: EmailUserRepository,
) {
    fun save(emailUser: EmailUser): EmailUser {
        return emailUserRepository.save(emailUser)
    }
}
