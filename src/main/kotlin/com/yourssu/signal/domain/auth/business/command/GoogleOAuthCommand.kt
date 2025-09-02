package com.yourssu.signal.domain.auth.business.command

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.auth.implement.EmailUser

data class GoogleOAuthCommand(
    val accessToken: String,
    val uuid: String,
    val email: String
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
    
    fun toDomain(): EmailUser {
        return EmailUser(
            uuid = uuid,
            email = email,
        )
    }
}
