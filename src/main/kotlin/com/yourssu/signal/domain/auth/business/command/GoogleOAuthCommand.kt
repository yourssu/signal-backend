package com.yourssu.signal.domain.auth.business.command

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.auth.implement.EmailUser

data class GoogleOAuthCommand(
    val code: String,
    val uuid: String,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
    
    fun toDomain(email: String): EmailUser {
        return EmailUser(
            uuid = uuid,
            email =  email,
        )
    }
}
