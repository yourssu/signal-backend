package com.yourssu.signal.domain.auth.implement

import com.yourssu.signal.domain.auth.storage.UserRepositoryImpl
import org.springframework.stereotype.Component

@Component
class UserWriter(
    private val userRepository: UserRepositoryImpl,
) {
    fun generateUser(): User {
        return userRepository.save(user = User())
    }
}
