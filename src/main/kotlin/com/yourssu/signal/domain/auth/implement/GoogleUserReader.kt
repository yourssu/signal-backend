package com.yourssu.signal.domain.auth.implement

import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.stereotype.Component

@Component
class GoogleUserReader(
    private val googleUserRepository: GoogleUserRepository,
) {
    fun existsByIdentifierAndUuid(identifier: String, uuid: Uuid): Boolean {
        return googleUserRepository.existsBy(identifier, uuid)
    }

    fun findUuidByIdentifier(identifier: String): Uuid? {
        return googleUserRepository.findUuidByIdentifier(identifier)
    }
}
