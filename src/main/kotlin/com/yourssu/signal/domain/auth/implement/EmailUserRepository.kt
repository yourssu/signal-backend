package com.yourssu.signal.domain.auth.implement

import com.yourssu.signal.domain.common.implement.Uuid

interface EmailUserRepository {
    fun save(emailUser: EmailUser): EmailUser
    fun findUuidByEmail(email: String): Uuid?
    fun existsBy(email: String, uuid: Uuid): Boolean
}
