package com.yourssu.signal.domain.viewer.business.command

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.domain.Gender

class VerificationCommand(
    val uuid: String,
    val gender: String? = null,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }

    fun toGender(): Gender? {
        if (gender == null) {
            return null
        }
        return Gender.of(gender)
    }
}
