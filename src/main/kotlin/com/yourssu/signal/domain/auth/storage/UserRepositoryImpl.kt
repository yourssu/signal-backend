package com.yourssu.signal.domain.auth.storage

import com.yourssu.signal.domain.auth.implement.User
import com.yourssu.signal.domain.auth.implement.UserRepository
import com.yourssu.signal.domain.auth.storage.exception.NotFoundUserException
import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl(
    private val jpaRepository: JpaUserRepository,
): UserRepository {
    override fun save(user: User): User {
        val entity = jpaRepository.save(UserEntity.from(user))
        return entity.toDomain()
    }

    override fun getByUuid(uuid: Uuid): User {
        return jpaRepository.findByUuid(uuid.value)
            ?.toDomain()
            ?: throw NotFoundUserException()
    }
}

interface JpaUserRepository: JpaRepository<UserEntity, Long> {
    fun findByUuid(uuid: String): UserEntity?
}
