package com.yourssu.signal.domain.auth.implement

import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.stereotype.Component

@Component
class EmailUserReader(
    private val emailUserRepository: EmailUserRepository,
) {
    fun existsByEmailAndUuid(email: String, uuid: Uuid): Boolean {
        return emailUserRepository.existsBy(email, uuid)
    }

    fun findUuidByEmail(email: String): Uuid? {
        return emailUserRepository.findUuidByEmail(email)
    }
}
