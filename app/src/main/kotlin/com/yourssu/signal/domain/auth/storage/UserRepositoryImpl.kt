package com.yourssu.signal.domain.auth.storage

import com.yourssu.signal.domain.user.implement.User
import com.yourssu.signal.domain.user.implement.UserRepository
import com.yourssu.signal.domain.auth.storage.exception.NotFoundUserException
import com.yourssu.signal.domain.common.implement.Uuid
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
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

    override fun lockByUuid(uuid: Uuid) {
        jpaRepository.lockByUuid(uuid.value) ?: throw NotFoundUserException()
    }
}

interface JpaUserRepository: JpaRepository<UserEntity, Long> {
    fun findByUuid(uuid: String): UserEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u.id from UserEntity u where u.uuid = :uuid")
    fun lockByUuid(uuid: String): Long?
}
