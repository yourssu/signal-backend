package com.yourssu.signal.domain.auth.implement

import com.yourssu.signal.domain.common.implement.Uuid

interface GoogleUserRepository {
    fun save(googleUser: GoogleUser): GoogleUser
    fun findUuidByIdentifier(identifier: String): Uuid?
    fun existsBy(uuid: Uuid): Boolean
}
