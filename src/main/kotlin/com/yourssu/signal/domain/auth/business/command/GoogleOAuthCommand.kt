package com.yourssu.signal.domain.auth.business.command

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.auth.implement.GoogleUser

class GoogleOAuthCommand(
    val code: String,
    val uuid: String,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
    
    fun toDomain(identifier: String): GoogleUser {
        return GoogleUser(
            uuid = uuid,
            identifier =  identifier,
        )
    }
}
