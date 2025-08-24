package com.yourssu.signal.domain.auth.implement

import com.yourssu.signal.domain.common.implement.Uuid

interface UserRepository {
    fun save(user: User): User
    fun getByUuid(uuid: Uuid): User
}
