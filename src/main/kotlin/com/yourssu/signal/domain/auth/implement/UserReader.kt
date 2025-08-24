package com.yourssu.signal.domain.auth.implement

import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userRepository: UserRepository
) {
    fun getByUuid(uuid: Uuid): User {
        return userRepository.getByUuid(uuid)
    }
}
