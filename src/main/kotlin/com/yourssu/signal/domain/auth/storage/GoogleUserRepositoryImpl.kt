package com.yourssu.signal.domain.auth.storage

import com.yourssu.signal.domain.auth.implement.GoogleUser
import com.yourssu.signal.domain.auth.implement.GoogleUserRepository
import com.yourssu.signal.domain.auth.storage.GoogleUserEntity
import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class GoogleUserRepositoryImpl(
    private val jpaRepository: JpaGoogleUserRepository,
): GoogleUserRepository {
    override fun save(googleUser: GoogleUser): GoogleUser {
        val entity = jpaRepository.save(GoogleUserEntity.from(googleUser))
        return entity.toDomain()
    }
    
    override fun findUuidByIdentifier(identifier: String): Uuid? {
        return jpaRepository.findByIdentifier(identifier)
            ?.let { Uuid(it.uuid) }
    }

    override fun existsBy(uuid: Uuid): Boolean {
        return jpaRepository.existsByUuid(uuid.value)
    }
}

interface JpaGoogleUserRepository: JpaRepository<GoogleUserEntity, Long> {
    fun findByIdentifier(identifier: String): GoogleUserEntity?
    fun existsByIdentifierAndUuid(identifier: String, uuid: String): Boolean
    fun existsByUuid(uuid: String): Boolean
}
