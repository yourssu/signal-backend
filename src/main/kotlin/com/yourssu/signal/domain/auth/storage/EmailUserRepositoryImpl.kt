package com.yourssu.signal.domain.auth.storage

import com.yourssu.signal.domain.auth.implement.EmailUser
import com.yourssu.signal.domain.auth.implement.EmailUserRepository
import com.yourssu.signal.domain.auth.storage.domain.EmailUserEntity
import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class EmailUserRepositoryImpl(
    private val jpaRepository: JpaEmailUserRepository,
): EmailUserRepository {
    override fun save(emailUser: EmailUser): EmailUser {
        val entity = jpaRepository.save(EmailUserEntity.from(emailUser))
        return entity.toDomain()
    }
    
    override fun findUuidByEmail(email: String): Uuid? {
        return jpaRepository.findByEmail(email)
            ?.let { Uuid(it.uuid) }
    }
    
    override fun existsBy(email: String, uuid: Uuid): Boolean {
        return jpaRepository.existsByEmailAndUuid(email, uuid.value)
    }
}

interface JpaEmailUserRepository: JpaRepository<EmailUserEntity, Long> {
    fun findByEmail(email: String): EmailUserEntity?
    fun existsByEmailAndUuid(email: String, uuid: String): Boolean
}
