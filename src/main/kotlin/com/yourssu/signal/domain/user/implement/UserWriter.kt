package com.yourssu.signal.domain.user.implement

import com.yourssu.signal.domain.auth.storage.UserRepositoryImpl
import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.stereotype.Component

@Component
class UserWriter(
    private val userRepository: UserRepositoryImpl,
) {
    fun generateUser(): User {
        return userRepository.save(user = User())
    }

    fun generateUser(uuid: String): User {
        val user = User(uuid = Uuid(uuid))
        return userRepository.save(user = user)
    }
}
